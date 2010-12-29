package com.sofurry.base.classes;

import org.json.JSONObject;

import android.app.Activity;
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
import android.widget.GridView;
import android.widget.ListAdapter;

import com.sofurry.R;
import com.sofurry.R.id;
import com.sofurry.R.layout;
import com.sofurry.base.interfaces.IManagedActivity;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.tempstorage.ManagerStore;

/**
 * @author SoFurry
 *
 * Class that is used as a base for all GalleryViews
 *
 * @param <T>
 */
public abstract class AbstractContentGallery<T> extends Activity implements IManagedActivity<T> {

	protected long uniqueKey = 0;  // The key to be used by the storage manager to recognize this particular activity

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#getUniqueKey()
	 */
	public long getUniqueKey() {
		return uniqueKey;
	}

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#setUniqueKey(long)
	 */
	public void setUniqueKey(long key) {
		uniqueKey = key;
	}

	protected ActivityManager<T> man = new ActivityManager<T>(this);
	private boolean finished = false;
	
	protected GridView galleryView;
	protected ListAdapter myAdapter;
	
	public ActivityManager<T> getActivityManager() {
		return man;
	}
	
	// Get parameters and initiate data fetch thread
	@SuppressWarnings("unchecked")
	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallerylayout);
		galleryView = (GridView) findViewById(R.id.galleryview);
		
		// See if the UID needs restoring
		ActivityManager.onCreateRefresh(this, savedInstanceState);
		
		if (ManagerStore.isStored(this)) {
		    man = ManagerStore.retrieve(this);
		    plugInAdapter();
		} else {
			man = new ActivityManager<T>(this);
			man.onActCreate();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		man.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		if (!finished) ManagerStore.store(this);
		super.onPause();
	}

	/**
	 * Creates a new adapter and plugs it into the gridview
	 */
	public void plugInAdapter() {
		int lastScrollY = galleryView.getFirstVisiblePosition();
		myAdapter = getAdapter(this);
		galleryView.setAdapter(myAdapter);
		galleryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView parentView, View childView, int position, long id) {
				setSelectedIndex(position);
			}
		});
		galleryView.setOnScrollListener(new OnScrollListener() {
			public void onScroll(final AbsListView view, final int first, final int visible, final int total) {
				man.onScroll(view, first, visible, total);
			}

			public void onScrollStateChanged(AbsListView view, int arg1) {
			}
		});
		
		galleryView.setSelection(lastScrollY);
	    man.hideProgressDialog();
	}


	/* Creates the menu items */
	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#onCreateOptionsMenu(android.view.Menu)
	 */
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
	

	/**
	 * Closes this list and returns to the main menu
	 */
	private void closeList() {
		man.closeList();
	}

	/**
	 * Displays the list on screen
	 */
	public void updateView() {
		galleryView.invalidateViews();
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


	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#setSelectedIndex(int)
	 */
	public abstract void setSelectedIndex(int selectedIndex);

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#getFetchParameters(int, int)
	 */
	public abstract AjaxRequest getFetchParameters(int page, int source);

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#getAdapter(android.content.Context)
	 */
	public abstract BaseAdapter getAdapter(Context context);

	
	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#parseResponse(org.json.JSONObject)
	 */
	public abstract void parseResponse(JSONObject obj) throws Exception;
	
	public abstract void resetViewSourceExtra(int newViewSource);

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#finish()
	 */
	@Override
	public void finish() {
		super.finish();
		finished = true;
		man.stopThumbDownloader();
		ManagerStore.retrieve(this); // Clean up manager store, so we don't have unused items laying aaround
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	finish();
            //return true;
        }

        return super.onKeyDown(keyCode, event);
    }


}
