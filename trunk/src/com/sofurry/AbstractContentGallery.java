package com.sofurry;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.sofurry.requests.AjaxRequest;

/**
 * @author SoFurry
 *
 * Class that is used as a base for all GalleryViews
 *
 * @param <T>
 */
public abstract class AbstractContentGallery<T> extends Activity implements IManagedActivity<T> {

	protected ActivityManager<T> man = new ActivityManager<T>(this);
	
	protected GridView galleryView;
	protected ListAdapter myAdapter;


	// Get parameters and initiate data fetch thread
	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallerylayout);
		galleryView = (GridView) findViewById(R.id.galleryview);
		man.onActCreate();
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
		man.stopThumbDownloader();
	}

}
