package com.sofurry.model;

import java.util.ArrayList;
import java.util.Collection;

import com.sofurry.base.interfaces.IAddObjectCallback;
import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.storage.NetworkListStorage;

import android.os.Handler;
import android.util.Log;


/**
 * Abstract network loaded list of objects. 
 * Provide simple get(index) access to objects loaded from network page splitted lists like list of submissions or list of comments
 * Implements automatic next page load and forward next page load
 *  
 * @author Night_Gryphon
 *
 * @param <T>
 */
public abstract class NetworkList<T> extends ArrayList<T> implements ICanCancel, IAddObjectCallback<T> {
		
//	private static final long serialVersionUID = 1L;
	
		private int defaultSize = Integer.MAX_VALUE;
		private boolean getSizeTriggerFirstLoad = false;

		public NetworkList() {
			super();
			NetworkListStorage.store(this);
		}

		public NetworkList(int defaultSize, boolean getSizeTriggerFirstLoad) {
			this();
			
			this.defaultSize = defaultSize;
			this.getSizeTriggerFirstLoad = getSizeTriggerFirstLoad;
		}
		
		@Override
		public void finalize() throws Throwable {
//			if (! isWorkerThread) // do not remove id from storage when worker thread die
				NetworkListStorage.remove(getListId());
			// should we destroy all list objects here or GC will do this job for us?
			fLoadingStatusListener = null;
			clear();
			super.finalize();
		}

//		private ArrayList<T> fList = new ArrayList<T>();
		
		// async loading next page
		private AsyncPageLoader fAsyncLoader = null;
		
		/**
		 * flag to determine if we are in worker thread or in main thread
		 */
//		private Boolean fLoading = false;

		public int preloadCount = 0; // amount of items till the end of currently loaded list to start forward next page preload
		
		/**
		 * Network List worker thread class 
		 */
		private class AsyncPageLoader extends Thread implements IJobStatusCallback, ICanCancel {
			public volatile Handler mHandler = null;
			private NetworkList<T> fParent = null;
			private int itemsRequested = -1;
			private boolean isCancelled = false;
			
			public AsyncPageLoader(NetworkList<T> aParent) {
				this(aParent, -1);
			}
			
			public AsyncPageLoader(NetworkList<T> aParent, int itemsRequested) {
				super("NetList: Page Loader");
				Log.d("[NetList]", "--------------------------=== Create loader");
				this.fParent = aParent; // do fParent become a working copy of parent thread object when worker thread starts?
				this.mHandler = new Handler();
				this.itemsRequested = itemsRequested;
			}

			public void setItemsRequested(int numItems) {
				synchronized (this) {
					itemsRequested = numItems;
				}
			}
			
			@Override
			public void run() {
				Log.d("[NetList]", "=== Start Loader");
				fAsyncLoader = fParent.fAsyncLoader;

				try {
					// should we set fParent.fLoadingStatusListener = this ? and fix runnables to refer parent thread fLoadingStatusListener variable?
					
					onStart(this);
					do {
						fParent.doLoadNextPage(this); 
						
						onPageLoaded(itemsRequested);

						// page loaded. Should we continue loading more pages?
						synchronized (this) { // Synchronize with this instance of loader while finishing load
							// block change itemsRquested and check fAsyncLoader 
							// while handle current value and set fAsyncLoader
							if ( 	(isFinalPage()) ||
									(isCancelled) || 
									(itemsRequested <= 0) || // no multiple pages requested
									(sizeLoaded() >= itemsRequested) ) { // done loading requested amount of items
										onSuccess(this);
										fParent.fAsyncLoader = null;
										fAsyncLoader = null;
										Log.d("[NetList]", "=== End Loader success");
										return;
									}
						}
					} while (! isCancelled); // retry if finish conditions don't met and not cancelled

				} catch (Exception e) {
					synchronized (this) { // synchronise with this instance of loader while finishing load
						onError(fParent, e.getMessage());
						fParent.fAsyncLoader = null;
						fAsyncLoader = null;
						Log.d("[NetList]", "=== End Loader with error");
					}
				}
				
			}

			// post callback to parent thread
			public void onStart(Object job) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() { // should we use fParent here or just refer to parent object variable?
		                	fParent.doStartNotify(fParent);
		                }
		            });
				}
			}

			public void onPageLoaded(final int numItems) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() {
		                	fParent.doPageLoaded(numItems);
		                }
		            });
				}
			}

			public void onSuccess(Object job) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() {
		                	fParent.doSuccessNotify(fParent);
		                }
		            });
				}
			}

			public void onError(Object job, final String msg) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() {
		                	fParent.doErrorNotify(fParent, msg);
		                }
		            });
				}
			}

			public void onProgress(Object job, final int progress, final int total, final String msg) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() {
		                	fParent.doProgressNotify(fParent, progress, total, msg);
		                }
		            });
				}
			}

			public void cancel() {
				fParent.doCancel();
				mHandler = null;
			}

		} // end of class AsyncPageLoader

		/**
		 * action when page load finished
		 * @param numItems - total requested num items to load ( <=0 - load one page)
		 */
		protected void doPageLoaded(int numItems) {
			doProgressNotify(this, sizeLoaded(), numItems, "");
		}
		
		/**
		 * allow descendants to override load status actions
		 * @param job
		 */
		protected void doStartNotify(Object job) {
        	  if (fLoadingStatusListener != null) {
          		  fLoadingStatusListener.onStart(job);
          	  }
		}

		/**
		 * allow descendants to override load status actions
		 * @param job
		 */
		protected void doSuccessNotify(Object job) {
      	  	fFirstPage = false; // we are done loading a page so next page will not be not first
        	if (fLoadingStatusListener != null) {
        		  fLoadingStatusListener.onSuccess(job);
        	}
		}

		/**
		 * allow descendants to override load status actions
		 * @param job
		 * @param msg
		 */
		protected void doErrorNotify(Object job, final String msg) {
	      	  fFirstPage = false; // we are done loading a page so next page will not be not first
	      	  if (fLoadingStatusListener != null) {
	      		  fLoadingStatusListener.onError(job, msg);
	      	  }
        }

		/**
		 * allow descendants to override load status actions
		 * @param job
		 * @param progress
		 * @param total
		 * @param msg
		 */
		protected void doProgressNotify(Object job, final int progress, final int total, final String msg) {
	      	  if (fLoadingStatusListener != null) {
	      		  fLoadingStatusListener.onProgress(job, progress, total, msg);
	      	  }
        }

		/**
		 * Hack to be able to call super.add from inside runnable object
		 * @param index
		 * @param object
		 */
/*		private void doadd(int index, T object) {
			super.add(index, object);
		}
		
		@Override
		public void add(final int index, final T object) {
//			if ( (isWorkerThread) && (fAsyncLoader != null) && (fAsyncLoader.isAlive())) {
			if ( isLoading() ) { // when load thread is alive we assume all add calls are inter-thread
				if (fAsyncLoader.mHandler != null) {
					fAsyncLoader.mHandler.post(new Runnable() {
		                public void run() {
		          			doadd(index, object);
		                }
		            });
				}
			} else {
          		doadd(index, object);
			}
		}


		/**
		 * Hack to be able to call super.add from inside runnable object
		 * @param object
		 * @return
		 */
/*		private boolean doadd(T object) {
			return super.add(object);
		}

		@Override
		public boolean add(final T object) {
//			if ( (isWorkerThread) && (fAsyncLoader != null) && (fAsyncLoader.isAlive())) {
			if ( isLoading() ) { // when load thread is alive we assume all add calls are inter-thread
				if (fAsyncLoader.mHandler != null) {
					fAsyncLoader.mHandler.post(new Runnable() {
		                public void run() {
		          			doadd(object);
		                }
		            });
				}
				return true;
			} else {
          		return doadd(object);
			}
		}

		/**
		 * Hack to be able to call super.add from inside runnable object
		 * @param collection
		 * @return
		 */
/*		private boolean doaddAll(Collection<? extends T> collection) {
			return super.addAll(collection);
		}

		@Override
		public boolean addAll(final Collection<? extends T> collection) {
//			if ( (isWorkerThread) && (fAsyncLoader != null) && (fAsyncLoader.isAlive())) {
			if ( isLoading() ) { // when load thread is alive we assume all add calls are inter-thread
				if (fAsyncLoader.mHandler != null) {
					fAsyncLoader.mHandler.post(new Runnable() {
		                public void run() {
		          			doaddAll(collection);
		                }
		            });
				}
				return true;
			} else {
          		return doaddAll(collection);
			}
		}

		/**
		 * Hack to be able to call super.add from inside runnable object
		 * @param location
		 * @param collection
		 * @return
		 */
/*		private boolean doaddAll(int location, Collection<? extends T> collection) {
			return super.addAll(location, collection);
		}

		@Override
		public boolean addAll(final int location, final Collection<? extends T> collection) {
//			if ( (isWorkerThread) && (fAsyncLoader != null) && (fAsyncLoader.isAlive())) {
			if ( isLoading() ) { // when load thread is alive we assume all add calls are inter-thread
				if (fAsyncLoader.mHandler != null) {
					fAsyncLoader.mHandler.post(new Runnable() {
		                public void run() {
		          			doaddAll(location, collection);
		                }
		            });
				}
				return true;
			} else {
          		return doaddAll(location, collection);
			}
		}
/**/
	
		@Override
		public T get(int index) {
			return get(index, true);
		}

		/**
		 * Get submission from list. Start load pages as needed
		 * @param ASubmissionIndex
		 * @return
		 */
		public T get(int aIndex, boolean allowLoad)  {
			if (aIndex < super.size()) {
				if (super.size() - aIndex - 1 < preloadCount) {
					if ( (!isLoading()) && allowLoad ) {
						Log.d("[NetList]", ">>>>> NextPage request (preload): index="+aIndex+"   fAsyncLoader: "+(fAsyncLoader == null));
						AsyncLoadNextPage();
					}
				}
				
				try {
					return super.get(aIndex);
				} catch (Exception e) {
					return null;
				}
			} else if ( (!isLoading()) && allowLoad ) {
				Log.d("[NetList]", ">>>>> NextPage request (no item): index="+aIndex+"   fAsyncLoader: "+(fAsyncLoader == null));
				AsyncLoadNextPage();
			}
			
			return null;
		}

		@Override
		public boolean isEmpty() {
			return ( isFinalPage() && (!isLoading()) && super.isEmpty()); // can be incorrect if isFinalPage is not overrided
		}

		/**
		 * Override this if website API provide actual number of items to load
		 * Return infinity if size can not be determined
		 */
		@Override
		public int size() {
			// TODO non actual value is bad when pass list through serializible as it looks serializible does not support 'null' list items
			if (isFinalPage() && ( ! isLoading() ) )  // is all possible item pages done loading?
				return super.size(); // no more items to load, return number of items we already have in list
			else {
				if ( (getSizeTriggerFirstLoad) && (isFirstPage()) && (! isLoading()) )
					AsyncLoadNextPage();
				return (sizeLoaded() > defaultSize)? sizeLoaded() : defaultSize; // there may be infinite items left to load
			}
		}

		/**
		 * Return amount of items already loaded from server
		 * @return
		 */
		public int sizeLoaded() {
			return super.size();
		}
		
		/**
		 * Return if next page loading in progress
		 * @return
		 */
		public Boolean isLoading() {
//			return (fLoading) && (fAsyncLoader != null) && (fAsyncLoader.isAlive());
			
			if (fAsyncLoader != null) {
				synchronized (fAsyncLoader) {
					return (fAsyncLoader != null); // should we synchronize(fAsyncLoader) this? 
				}
			} else
				return false;
		}
		
		/**
		 * true if current page is last page and there is no more pages to download in current list
		 * @return
		 */
		public Boolean isFinalPage() {
			return false;
		}
		
		private Boolean fFirstPage = true;
		
		/**
		 * true if initial page loading. used to trigger different progress bars for first and other page loads
		 * @return
		 */
		public Boolean isFirstPage() {
			return fFirstPage;
		}
		

		/**
		 * Load items page in worker thread
		 */
		protected void AsyncLoadNextPage() {
			AsyncLoadNextPage(-1);
		}
		
		/**
		 * Load items page in worker thread until list contain less than numItems or EOF
		 */
		protected void AsyncLoadNextPage(int numItems) {
			if  ( (! isLoading()) && (! isFinalPage()) ) {
				fAsyncLoader = new AsyncPageLoader(this, numItems);
				fAsyncLoader.start();
			}
		}

		/**
		 * Cancel worker thread
		 */
		public void cancel() {
			if (isLoading()) {
				fAsyncLoader.cancel();
			}
		}
		
		/**
		 * perform synchronous loading of next page from server.
		 * Synchronous call. It's SubmissionsList who responsible to make this asynch
		 * @param StatusCallback - callback object to call correct methods to pass through threads boundary
		 */
		protected abstract void doLoadNextPage(IJobStatusCallback StatusCallback) throws Exception; 
		
		/**
		 * perform cancel load page request. Called inside worker thread.
		 */
		protected void doCancel() {
		}

		// ID of this list for ListsStorage
		private long fListId = System.currentTimeMillis();

		public long	getListId() {
			return fListId;
		}
		
		
		private IJobStatusCallback fLoadingStatusListener = null;
		
		public synchronized void setStatusListener(IJobStatusCallback ALoadStatusListener) {  // don't do this simultaneously with storing/restoring callback methods
				fLoadingStatusListener = ALoadStatusListener;
		}
		
		public synchronized void PreloadItems(int numItems) { // dont start until we finish set callback
			if (fAsyncLoader != null){
				synchronized (fAsyncLoader) { 
					//  if asyncLoader checking/finishing thread wait before isLoading() check
					// if we changing numItems then async loader should wait to start checks
					if (isLoading()) {
						// set numItems so AsyncLoader will continue
						if (fAsyncLoader != null)
							fAsyncLoader.setItemsRequested(numItems);
					} else {
						// start new loader
						AsyncLoadNextPage(numItems);
					}
				};
			} else
				AsyncLoadNextPage(numItems);
		}
}
