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
		if (fList == null)
			setList(createBrowseList());

		if (fDataView == null) 
			fDataView = getDataView();

		if (fDataView != null) {
			fDataView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView parentView, View childView, int position, long id) {
					onDataViewItemClick(position);
				}
			});
			fDataView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView parentView, View childView, int position, long id) {
					return onDataViewItemLongClick(position);
				}
			});

			// Plug in adapter
			if (myAdapter == null) {
				myAdapter = createAdapter(); 
				fDataView.setAdapter(myAdapter);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("ListID", fList.getListId());
		outState.putCharSequence("ActTitle", getTitle());

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		try {
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
		//detach current list
		if (fList != null) {
			fList.setStatusListener(null); // clear status callbacks before detach list
		}
		
		// attach new list
		fList = aList;
		
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
		refreshDataView();
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
			thumbLoader.cancel(false);
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
