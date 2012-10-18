package com.sofurry.base.classes;

import com.sofurry.AppConstants;
import com.sofurry.activities.SettingsActivity;
import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.helpers.ProgressBarHelper;
import com.sofurry.mobileapi.downloaders.ThumbnailDownloader;
import com.sofurry.mobileapi.downloadmanager.DownloadManager;
import com.sofurry.mobileapi.downloadmanager.HTTPFileDownloadTask;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;
import com.sofurry.storage.ImageStorage;
import com.sofurry.storage.NetworkListStorage;
import com.sofurry.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
	protected synchronized void refreshDataView() {
		if ((myAdapter != null) && (myAdapter instanceof BaseAdapter))
			((BaseAdapter) myAdapter).notifyDataSetChanged();

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
		
/*		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("startThumbLoader", false))
				LoadThumbnails();
		}/**/
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("ListID", fList.getListId());
		outState.putCharSequence("ActTitle", getTitle());

		if (fDataView != null)
			outState.putInt("SelectedIndex", fDataView.getFirstVisiblePosition());
		
/*		if (thumbLoader != null) {
//			StopLoadThumbnails();
			outState.putBoolean("startThumbLoader", true);
		}/**/
		
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		try {
			if (isFinishing()) {
//				StopLoadThumbnails();
				fList.finalize();
				fList = null;
			} else {
				// TODO should we clean callback for fList and ThumbDownloader here?
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null) {
			int jumptoitem = data.getIntExtra("JumpTo", -1);
			if ((jumptoitem >= 0) && (fDataView != null)) {
//				fDataView.requestFocusFromTouch();
//				if ( (jumptoitem < fDataView.getFirstVisiblePosition()) || (jumptoitem > fDataView.getLastVisiblePosition()))
					fDataView.setSelection(jumptoitem);
			}
		}
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
				
				public void onSuccess(Object job) {
					refreshDataView();
					onLoadFinish();
				}
				
				public void onStart(Object job) {
					onLoadStart();
				}
				
				// Called by SFSubmissionList on thumb loading progress
				public void onProgress(Object job, int progress, int total, String msg) {
					refreshDataView();
				}
				
				public void onError(Object job, String msg) {
					onLoadError(msg);
				}
			});
		}
	}
	
	private DownloadManager dlmanager = new DownloadManager(4); //TODO
	private int dlindex = -1;
	
	private void feedLoader() {
		Submission s = null;
		do {
			s = fList.get(dlindex, false);
			if (s == null)
				break;
			if (! s.isSubmissionFileExists())
				dlmanager.Download(new HTTPFileDownloadTask(
						s.getFullURL(), ImageStorage.getSubmissionImagePath(s.getCacheName()),
						null, 15, false, true, null, "text", 3));
			
			dlindex++;
		} while (s != null);
	}

	public void PreloadItems(final int numItems) {
		if (fList == null)
			return;

		if (dlindex <0) {

			// feed downloader with already loaded items
			dlindex = 0;
			feedLoader();
			
			fList.setStatusListener(new IJobStatusCallback() {
				public void onSuccess(Object job) {
					refreshDataView();
					onLoadFinish();
					setListCallback(); // reset callback to default
					
					feedLoader(); // feed rest of items
					setListCallback(); // restore callback
					dlindex = -1; // unlock new preloads
				}
				
				public void onStart(Object job) {
					onLoadStart();
				}
				
				// Called by SFSubmissionList on thumb loading progress
				public void onProgress(Object job, int progress, int total, String msg) {
					if (job instanceof NetworkList) { // progress from load page, skip thumb progress
						Log.d("[Preload]", "Item "+progress+" of "+total+"done");
						feedLoader();	// feed downloader on page loaded
					}
					refreshDataView();
				}
				
				public void onError(Object job, String msg) {
					onLoadError(msg);
					feedLoader();
					setListCallback();
					dlindex = -1;
				}
			});
		}
		

		if (fList.sizeLoaded() < numItems)
			fList.PreloadItems(numItems);
		
	}
	
	public void onLoadStart() {
		showProgressDialog("Loading page...", ! fList.isFirstPage());
	}
	
	public void onLoadFinish() {
		hideProgressDialog();
	}
	
	public void onLoadError(String msg) {
		hideProgressDialog();
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
		refreshDataView();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, AppConstants.MENU_SETTINGS, 10, "Settings").setIcon(android.R.drawable.ic_menu_preferences);

        SubMenu sub = menu.addSubMenu(0,0,20,"More").setIcon(android.R.drawable.ic_menu_more);
//        sub.add(0, AppConstants.MENU_CLEANTHUMB, 10, "Clean thumnails");
        sub.add(0, AppConstants.MENU_PRELOAD, 10, "Cache all");
//        sub.add(0, AppConstants.MENU_DOWNLOAD_ALL, 10, "Download all");
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
			case AppConstants.MENU_SETTINGS:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
				
			case AppConstants.MENU_PRELOAD:
				Integer i = Integer.parseInt(Utils.getPreferences(this).getString(AppConstants.PREFERENCE_PRELOAD_MAX, "200"));
				if (i != null)
					PreloadItems(i);
				return true;
				
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
	
}
