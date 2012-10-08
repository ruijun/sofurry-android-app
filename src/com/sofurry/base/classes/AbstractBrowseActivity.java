package com.sofurry.base.classes;

import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.helpers.ProgressBarHelper;
import com.sofurry.mobileapi.downloaders.ThumbnailDownloader;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;
import com.sofurry.storage.NetworkListStorage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Abstract activity to browse through list of submissions.
 * Automatically load thumbnails after page load
 * 
 * @author Night_Gryphon
 */
public abstract class AbstractBrowseActivity extends Activity {
	/**
	 * primary data list that will be presented to user
	 */
	protected NetworkList<Submission> fList = null;
	
	/**
	 * UI element to present NetworkList items to user
	 */
	@SuppressWarnings("rawtypes")
	protected AdapterView fDataView = null;
	
	/**
	 * return UI element to present NetworkList items to user
	 */
	@SuppressWarnings("rawtypes")
	protected abstract AdapterView getDataView();
	
	/**
	 * Refresh display to show changes in list
	 */
	protected void refreshDataView() {
		if (fDataView != null) {
			if (fDataView instanceof AbsListView)
				((AbsListView) fDataView).invalidateViews();
			else
				fDataView.invalidate(); // used instead of invalidateViews
			
		}
	}

	/**
	 * adapter to use in AdapterView
	 */
	protected Adapter myAdapter = null;
	
	/**
	 * create new adapter to be used in DataView
	 * @param context
	 * @return
	 */
	protected abstract Adapter createAdapter();
	
	/**
	 * progress messages and dialogs
	 */
	private ProgressBarHelper pbh = new ProgressBarHelper(this, new ICanCancel() {
		@Override
		public void cancel() {
			if (fList != null)
				fList.cancel();
		}
	});
	
	/**
	 * Create NetworkList that used in this activity
	 * @return
	 */
	protected abstract NetworkList<Submission> createBrowseList();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			setList(NetworkListStorage.get(savedInstanceState.getLong("ListID")));
			setTitle(savedInstanceState.getCharSequence("ActTitle"));
		} else {
		    Bundle extras = getIntent().getExtras();
		    if (extras != null) {
//		    	viewSource = ViewSource.valueOf(extras.getString("viewSource"));
//		    	viewSearch = extras.getString("viewSearch");
		    	setTitle(extras.getString("activityTitle"));
				setList(NetworkListStorage.get(extras.getInt("ListID")));
		    }
		}
	}



	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		
		// required descendant specific params should be loaded before this point so now we can request DataView and Adapter
		if (fDataView == null) 
			fDataView = getDataView();

		if (fList == null)
			setList(createBrowseList());

		if (fDataView != null) {
			// TODO set forward preload count to fList
			
			fDataView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView parentView, View childView, int position, long id) {
					if ( (fList != null) && (fList.get(position) != null))
						onDataViewItemClick(position);
				}
			});
			fDataView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
					if ( (fList != null) && (fList.get(position) != null))
						return onDataViewItemLongClick(position);
					else
						return false;
				}
			});

			pluginAdapter();
			
			if (savedInstanceState != null) {
				fDataView.setSelection(savedInstanceState.getInt("", 0));
			} else {
			    Bundle extras = getIntent().getExtras();
			    if (extras != null) {
					fDataView.setSelection(extras.getInt("SelectedIndex", 0));
			    }
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("ListID", fList.getListId());
		outState.putCharSequence("ActTitle", getTitle());

		if (fDataView != null)
			outState.putInt("SelectedIndex", fDataView.getFirstVisiblePosition());
		
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		try {
			StopLoadThumbnails();
			fList.finalize();
			fList = null;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	/**
	 * Called when item is selected in list/gallery
	 * @param aItemIndex
	 */
	protected void onDataViewItemClick(int aItemIndex) {
	}

	/**
	 * Called when item is long clicked list/gallery
	 * @param aItemIndex
	 */
	protected boolean onDataViewItemLongClick(int aItemIndex) {
		return false;
	}
	
	/**
	 * Plug in NetworkList to browse in this activity and call refreshDataView
	 * @param aList
	 */
	public void setList(NetworkList<Submission> aList) {
		//detach current list and unplug adapter
		if (fList != null) {
			try {
				if (fDataView != null)
					fDataView.setAdapter(null);
				
				myAdapter = null; // is this enough to destroy object?
//				if (myAdapter != null)
				
				fList.setStatusListener(null); // clear status callbacks before detach list
				fList.finalize();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			fList = null;
		}
		
		// attach new list
		fList = aList;
		
		if (fList != null) {
			setListCallback();
			pluginAdapter();
		}
	}

	/**
	 * Set callback procs to fList
	 */
	protected void setListCallback() {
		if (fList != null) {
			fList.setStatusListener(new IJobStatusCallback() {
				
				@Override
				public void onSuccess(Object job) {
					hideProgressDialog();
					refreshDataView();
					LoadThumbnails();
				}
				
				@Override
				public void onStart(Object job) {
					showProgressDialog("Loading page...", ! fList.isFirstPage());
				}
				
				@Override
				public void onProgress(Object job, int progress, int total, String msg) {
//					doRefreshList();
				}
				
				@Override
				public void onError(Object job, String msg) {
					hideProgressDialog();
				}
			});
		}
	}
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
//		if (fList != null)
//			fList.setStatusListener(null); // clear callback so another activity can use list
	}

	@Override
	protected void onResume() {
		super.onResume();
		setListCallback(); // restore callbacks to our activity
	}

	/**
	 * Assign adapter to fDataView and reset list position
	 */
	public void pluginAdapter() {
		// Plug in adapter
		if ((fDataView != null) && (myAdapter == null)) {
			myAdapter = createAdapter(); 
			fDataView.setAdapter(myAdapter);
			fDataView.setSelection(0);
			
			refreshDataView();
		}
	}
	/**
	 * current thumbnail loader
	 */
	private ThumbnailDownloader thumbLoader = null;
	
	/**
	 * Start or restart thumbnails loading 
	 */
	protected void LoadThumbnails() {
		StopLoadThumbnails();
		
		thumbLoader = new ThumbnailDownloader() {
			@Override
			protected void onProgressUpdate(Integer... values) {
				refreshDataView();
			}
		};
		thumbLoader.execute(fList);
	}
	
	/**
	 * Stop loading thumbnails
	 */
	protected void StopLoadThumbnails() {
		if (thumbLoader != null)
			if (thumbLoader.cancel(false))
				thumbLoader = null;
	}
	
	/**
	 * Shows the progress Dialog
	 * @param msg
	 */
	public void showProgressDialog(String msg, Boolean toast) {
		if (toast)
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		else
			pbh.showProgressDialog(msg);
	}
	
	/**
	 * Hides the progress Dialog
	 */
	public void hideProgressDialog() {
		pbh.hideProgressDialog();
	}
}
