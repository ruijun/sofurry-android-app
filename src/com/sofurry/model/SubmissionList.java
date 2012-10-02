package com.sofurry.model;

import java.util.ArrayList;

import com.sofurry.base.interfaces.IAddSubmission;
import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.storage.SubmissionListStorage;

import android.os.Handler;


// abstract list of submissions for any online gallery
public abstract class SubmissionList implements IAddSubmission, ICanCancel {
		
	
		public SubmissionList() {
			super();
			SubmissionListStorage.store(this);
		}

		@Override
		protected void finalize() throws Throwable {
			SubmissionListStorage.remove(getListId());
			super.finalize();
		}

		private ArrayList<Submission> fSubmissionsList = new ArrayList<Submission>();
		
		// async loading next page
		private AsyncPageLoader fAsyncLoader = null;

		private int preloadCount = 0; // amount of items till the end of currently loaded list to start forward next page preload
		
		// class AsyncPageLoader
		private class AsyncPageLoader extends Thread implements IJobStatusCallback, ICanCancel {
			public volatile Handler mHandler = null;
			private SubmissionList fParent = null;
			
			public AsyncPageLoader(SubmissionList aParent) {
				super();
				this.fParent = aParent;
				this.mHandler = new Handler();
			}

			@Override
			public void run() {
				try {
//					fParent.
					doLoadNextPage(this); // should we pass fParent here to check cancelLoadFlag between different threads?
				} catch (Exception e) {
					onError(fParent, e.getMessage());
				}
				
				fParent.fAsyncLoader = null;
			}

			// post callback to parent thread
			@Override
			public void onStart(Object job) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() {
		              	  if (fParent.fLoadingStatusListener != null) {
		              		  fParent.fLoadingStatusListener.onStart(fParent);
		              	  }
		                }
		            });
				}
			}

			@Override
			public void onFinish(Object job) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() {
		              	  if (fParent.fLoadingStatusListener != null) {
		              		  fParent.fLoadingStatusListener.onFinish(fParent);
		              	  }
		                }
		            });
				}
			}

			@Override
			public void onError(Object job, final String msg) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() {
		              	  if (fParent.fLoadingStatusListener != null) {
		              		  fParent.fLoadingStatusListener.onError(fParent, msg);
		              	  }
		                }
		            });
				}
			}

			@Override
			public void onProgress(Object job, final int progress, final int total, final String msg) {
				if (mHandler != null) {
		            mHandler.post(new Runnable() {
		                public void run() {
		              	  if (fParent.fLoadingStatusListener != null) {
		              		  fParent.fLoadingStatusListener.onProgress(fParent, progress, total, msg);
		              	  }
		                }
		            });
				}
			}

			@Override
			public void cancel() {
				fParent.doCancel();
				mHandler = null;
			}

		} // end of class AsyncPageLoader
		
		/**
		 * add submission to list. 
		 * if there is AsyncLoader working - assume we are called from worker thread so post runnable to main thread. 
		 */
		public void AddSubmission(final Submission sub) {
			if ( (fAsyncLoader != null) && (fAsyncLoader.isAlive())) {
				if (fAsyncLoader.mHandler != null) {
					fAsyncLoader.mHandler.post(new Runnable() {
		                public void run() {
		              	  if (fSubmissionsList != null) {
		              		  fSubmissionsList.add(sub);
		              	  }
		                }
		            });
				}
			} else {
            	  if (fSubmissionsList != null) {
              		  fSubmissionsList.add(sub);
              	  }
			}
		}

		/**
		 * Get submission from list. Start load pages as needed
		 * @param ASubmissionIndex
		 * @return
		 * @throws Exception
		 */
		public Submission getSubmission(int ASubmissionIndex) throws Exception  {
			if (ASubmissionIndex < fSubmissionsList.size()) {
				if (fSubmissionsList.size() - ASubmissionIndex - 1 < preloadCount) {
					AsyncLoadNextPage();
				}
				return fSubmissionsList.get(ASubmissionIndex);
			} else if ((fAsyncLoader == null) || ( !fAsyncLoader.isAlive() )) {
				if ( 	IsFinalPage() || 
						( (getSubmissionsCount() >=0 ) && (ASubmissionIndex >= getSubmissionsCount())) )  {
					throw new Exception("Submission index out of range");
				}
				AsyncLoadNextPage();
			}
			return null;
		}
		
		// no sync request because we can't stop main thread
		/*
		private void SyncLoadNextPage() {
			if (fAsyncLoader != null) {
				// wait for semaphore
			} else {
				try {
	              	  if (fLoadingStatusListener != null) {
	              		  fLoadingStatusListener.onStart(this);
	              	  }

	              	  doLoadNextPage();
	              	  
	              	  if (fLoadingStatusListener != null) {
	              		  fLoadingStatusListener.onFinish(this);
	              	  }
				} catch (Exception e) {
	              	  if (fLoadingStatusListener != null) {
	              		  fLoadingStatusListener.onError(this);
	              	  }
				}
			}
		}
		*/
		
		private void AsyncLoadNextPage() {
			if  ( (fAsyncLoader == null) || ( ! fAsyncLoader.isAlive() ) ) {
				fAsyncLoader = new AsyncPageLoader(this);
				fAsyncLoader.start();
			}
		}

		/**
		 * Cancel worker thread
		 */
		public void cancel() {
			if ((fAsyncLoader != null) && (fAsyncLoader.isAlive())) {
				fAsyncLoader.cancel();
			}
		}
		
		/**
		 * perform synchronous loading of next page from server.
		 * Synchronous call. It's SubmissionsList who responsible to make this asynch
		 * @param StatusCallback
		 */
		protected abstract void doLoadNextPage(IJobStatusCallback StatusCallback); 
		
		/**
		 * perform cancel load page request. Called inside worker thread.
		 */
		protected abstract void doCancel(); 

		protected Boolean IsFinalPage() { // true if there is no more pages to download in current list
			return true;
		}
		
		protected int getSubmissionsCount() { // -1 = unknown number of submissions
			return -1;
		}
		
		// ID of this list for ListsStorage
		private long fListId = System.currentTimeMillis();

/*		public void setListId(long AListId) { // can be useful when restoring list from storage on activity restoring. ???
			fListId = AListId;
		} */
		
		public long	getListId() {
			return fListId;
		}
		
		
		private IJobStatusCallback fLoadingStatusListener = null;

		public void setStatusListener(IJobStatusCallback ALoadStatusListener) {
			fLoadingStatusListener = ALoadStatusListener;
		}
}
