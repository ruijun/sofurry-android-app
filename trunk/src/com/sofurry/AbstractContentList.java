package com.sofurry;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.AbsListView.OnScrollListener;

import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.RequestHandler;
import com.sofurry.requests.RequestThread;
import com.sofurry.util.ErrorHandler;

/**
 * @author SoFurry
 *
 * Class that is used as a base for all ListViews
 * 
 * @param <T>
 */
public abstract class AbstractContentList<T> extends ListActivity implements IContentActivity {

	private ProgressDialog pd;
	protected int numResults;
	protected ArrayList<T> resultList;
	protected ThumbnailDownloaderThread thumbnailDownloaderThread;
	private RequestThread listRequester = null;
	protected int currentPage = 0;
	protected int viewSource = AppConstants.VIEWSOURCE_ALL;
	protected String viewSearch = "";
	protected int lastScrollY = 0;

	/**
	 * The request handler to be used to handle the feedback from the AjaxRequest
	 */
	protected RequestHandler requesthandler = new RequestHandler() {
		
		@Override
		public void onError(int id,Exception e) {
			closeList();
			ronError(e);
		}
		
		@Override
		public void onData(int id,JSONObject obj) {
			resultList = new ArrayList<T>();
			parseResponse(obj);
			if (pd != null && pd.isShowing())
			  pd.dismiss();
 		      updateView();
		}

		@Override
		public void refresh() {
			updateContentList();
		}
		
	};
	
	/**
	 * Is called when an error occurs in the asyncronus thread
	 * @param e
	 */
	public void ronError(Exception e) {
		ErrorHandler.showError(this, e);
	}

	// Get parameters and initiate data fetch thread
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadPage(currentPage, viewSource, true);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		AbstractContentGallery.createBrowsableMenu(menu);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_FILTER_KEYWORDS:
			Intent intent = new Intent(this, TagEditor.class);
			startActivityForResult(intent, AppConstants.ACTIVITY_TAGS);
			return true;
		case AppConstants.MENU_FILTER_ALL:
			resetViewSource(AppConstants.VIEWSOURCE_ALL);
			return true;
		case AppConstants.MENU_FILTER_FEATURED:
			resetViewSource(AppConstants.VIEWSOURCE_FEATURED);
			return true;
		case AppConstants.MENU_FILTER_FAVORITES:
			resetViewSource(AppConstants.VIEWSOURCE_FAVORITES);
			return true;
		case AppConstants.MENU_FILTER_WATCHLIST:
			resetViewSource(AppConstants.VIEWSOURCE_WATCHLIST);
			return true;
		case AppConstants.MENU_FILTER_GROUP:
			resetViewSource(AppConstants.VIEWSOURCE_GROUP);
			return true;
		case AppConstants.MENU_FILTER_WATCHLIST_COMBINED:
			resetViewSource(AppConstants.VIEWSOURCE_WATCHLIST_COMBINED);
			return true;
		default:
			return false;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Handles the return value from TAGEditor
		if (requestCode == AppConstants.ACTIVITY_TAGS) {
			if (data == null) return;
			viewSearch = data.getStringExtra("tags");
			resetViewSource(AppConstants.VIEWSOURCE_SEARCH);
			return;
		} 
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void loadPage(int pageNum, int source, boolean showLoadingScreen) {
		if (thumbnailDownloaderThread != null) {
			thumbnailDownloaderThread.stopThread();
			thumbnailDownloaderThread = null;
		}
		if (showLoadingScreen)
			pd = ProgressDialog.show(this, "Fetching data...", "Please wait", true, false);
		
		AjaxRequest ar = getFetchParameters(pageNum, source);
		listRequester = ar.execute(requesthandler); // Requests the data, and will redirect results to this object
	}
	
	
	// Goes back to the main menu
	private void closeList() {
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);
		finish();
	}

	// Sets the resulting list on the screen
	private void updateView() {
		if (resultList == null)
			return;

		lastScrollY = getListView().getFirstVisiblePosition();
		Log.i("SF AbstractContentList", "updateView called, last scrollpos: "+lastScrollY);
		listRequester = null;
		setListAdapter(getAdapter(this));
		getListView().setTextFilterEnabled(true);
		// bind a selection listener to the view
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView parentView, View childView, int position, long id) {
				setSelectedIndex(position);
			}
		});
	    getListView().setOnScrollListener(new OnScrollListener() {
	        public void onScroll(final AbsListView view, final int first,
	                                    final int visible, final int total) {
	            // detect if last item is visible
	            if (visible < total && (first + visible == total) && listRequester == null) {
	                Log.d("OnScrollListener - end of list", "fvi: " +
	                   first + ", vic: " + visible + ", tic: " + total);
	                currentPage++;
	        		loadPage(currentPage, viewSource, false);
	            }
	        }

			public void onScrollStateChanged(AbsListView view, int arg1) {
			}
	    }); 
		Log.i("SF", "Scrolling TO: "+lastScrollY);
	    getListView().setSelection(lastScrollY);

	}

	public abstract void setSelectedIndex(int selectedIndex);

	public abstract AjaxRequest getFetchParameters(int page, int source);

	protected abstract ListAdapter getAdapter(Context context);

	public abstract void resetViewSource(int newViewSource);
	
	public abstract void parseResponse(JSONObject obj);

	protected void updateContentList() {
		updateView();
	}

	
	@Override
	public void finish() {
		super.finish();
		if (thumbnailDownloaderThread != null)
			thumbnailDownloaderThread.stopThread();
	}
	
}
