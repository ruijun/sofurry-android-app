package com.sofurry;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.sofurry.model.IHasThumbnail;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.RequestHandler;
import com.sofurry.requests.RequestThread;
import com.sofurry.requests.ThumbnailDownloaderThread;
import com.sofurry.util.ErrorHandler;

/**
 * @author SoFurry
 *
 * Class that is used as a base for all ListViews
 * 
 * @param <T>
 */
public abstract class AbstractContentList<T> extends ListActivity implements IManagedActivity<T> {

	protected ActivityManager<T> man = null;
	private boolean finished = false;

	// Get parameters and initiate data fetch thread
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ManagerStore.isStored(this)) {
		    man = ManagerStore.retrieve(this);
		    plugInAdapter();
		} else {
			man = new ActivityManager<T>(this);
			man.onActCreate();
		}
		
	}
	
	public ActivityManager<T> getActivityManager() {
		return man;
	}
	
	@Override
	protected void onPause() {
		if (!finished) ManagerStore.store(this);
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		man.createBrowsableMenu(menu);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (man.onOptionsItemSelected(item))
			return true;
		else
			return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (man.onActivityResult(requestCode, resultCode, data)) return;
		super.onActivityResult(requestCode, resultCode, data);
	}

	// Goes back to the main menu
	private void closeList() {
		man.closeList();
	}
	
	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#plugInAdapter()
	 * 
	 * Creates the plugin adapter
	 */
	public void plugInAdapter() {
		int lastScrollY = getListView().getFirstVisiblePosition();
		Log.i("SF AbstractContentList", "updateView called, last scrollpos: "+lastScrollY);

		setListAdapter(getAdapter(this));
		getListView().setTextFilterEnabled(true);
		// bind a selection listener to the view
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView parentView, View childView, int position, long id) {
				man.stopThumbDownloader();
				setSelectedIndex(position);
			}
		});
	    getListView().setOnScrollListener(new OnScrollListener() {
	        public void onScroll(final AbsListView view, final int first,final int visible, final int total) {
	        	man.onScroll(view, first, visible, total);
	        }

			public void onScrollStateChanged(AbsListView view, int arg1) {
			}
	    }); 
	    getListView().setSelection(lastScrollY);
	    getListView().invalidateViews();
	    man.hideProgressDialog();
	}

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#resetViewSourceExtra(int)
	 * 
	 * Everything that needs to be done, but is not done by the ActivityManager
	 */
	public void resetViewSourceExtra(int newViewSource) {
		// Intentionally left blank
	}

	// Sets the resulting list on the screen
	public void updateView() {
		//plugInAdapter();
		getListView().invalidateViews();
		
		Log.i("SF", "Refresh");
	}

	/**
	 * Returns an item of the resultlist at the specified index
	 * @param idx
	 * The index to return from
	 * @return
	 * Returns the item of the instanced type
	 */
	public T getDataItem(int idx) {
		T temp = man.getResultList().get(idx);
		return temp;
	}
	
	public abstract void setSelectedIndex(int selectedIndex);

	public abstract AjaxRequest getFetchParameters(int page, int source);

	public abstract BaseAdapter getAdapter(Context context);

	public abstract void parseResponse(JSONObject obj) throws Exception;

	protected void updateContentList() {
		updateView();
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	finish();
            //return true;
        }

        return super.onKeyDown(keyCode, event);
    }


	@Override
	public void finish() {
		super.finish();
		finished = true;
		man.stopThumbDownloader();
		ManagerStore.retrieve(this); // Clean up manager store, so we don't have unused items laying aaround
	}
	
}
