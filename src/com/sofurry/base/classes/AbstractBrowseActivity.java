package com.sofurry.base.classes;

import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.helpers.ProgressBarHelper;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;
import com.sofurry.storage.NetworkListStorage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

/**
 * Abstract activity to browse through list of submissions
 * @author Night_Gryphon
 *
 */
public abstract class AbstractBrowseActivity extends Activity {
	/**
	 * primary data list that will be presented to user
	 */
	protected NetworkList<Submission> fList = null;
	
	/**
	 * UI element to present NetworkList items to user
	 */
	protected AdapterView fDataView = null;
	
	/**
	 * return UI element to present NetworkList items to user
	 */
	protected abstract AdapterView getDataView();
	
	private Adapter myAdapter = null;
	
	/**
	 * create new adapter to be used in DataView
	 * @param context
	 * @return
	 */
	public abstract Adapter getAdapter(Context context);
	
	/**
	 * progress messages and dialogs
	 */
	private ProgressBarHelper pbh = new ProgressBarHelper(this, new ICanCancel() {
		@Override
		public void cancel() {
			fList.cancel();
		}
	});
	
	/**
	 * Create NetworkList that used in this activity
	 * @return
	 */
	protected abstract NetworkList<Submission> getBrowseList();
	
	/**
	 * Refresh display to show changes in list
	 */
	protected abstract void doRefreshList();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null)
			setList(NetworkListStorage.get(savedInstanceState.getLong("ListID")));
		
		if (fList == null)
			setList(getBrowseList());
		
		// Plug in adapter
		myAdapter = getAdapter(this);
		fDataView.setAdapter(myAdapter);
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

		// TODO  set title
		// TODO read params
	}

	/**
	 * Called when item is selected in list/gallery
	 * @param aItemIndex
	 */
	protected abstract void onDataViewItemClick(int aItemIndex);

	/**
	 * Called when item is long clicked list/gallery
	 * @param aItemIndex
	 */
	protected boolean onDataViewItemLongClick(int aItemIndex) {
		return false;
	}
	
	/**
	 * Plug in NetworkList to browse in this activity
	 * @param aList
	 */
	public void setList(NetworkList<Submission> aList) {
		//detach current list
		if (fList != null) {
			fList.setStatusListener(null); // clear status callbacks before detach list
		}
		
		// attach new list
		fList = aList;
		
		if (fList == null)
			return;
		
		fList.setStatusListener(new IJobStatusCallback() {
			
			@Override
			public void onSuccess(Object job) {
				hideProgressDialog();
				LoadThumbnails();
			}
			
			@Override
			public void onStart(Object job) {
				showProgressDialog("Loading page...", fList.isFirstPage());
			}
			
			@Override
			public void onProgress(Object job, int progress, int total, String msg) {
			}
			
			@Override
			public void onError(Object job, String msg) {
				hideProgressDialog();
			}
		});
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("ListID", fList.getListId());

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		((BaseAdapter) myAdapter).notifyDataSetChanged();
		super.onResume();
	}

	/**
	 * Start or restart thumbnails loading 
	 */
	protected void LoadThumbnails() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Shows the progress Dialog
	 * @param msg
	 */
	public void showProgressDialog(String msg, Boolean toast) {
		if (!toast)
			pbh.showProgressDialog(msg);
		else
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Hides the progress Dialog
	 */
	public void hideProgressDialog() {
		pbh.hideProgressDialog();
	}

}
