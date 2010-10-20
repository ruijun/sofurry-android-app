package com.sofurry;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.AbsListView;
import android.widget.Toast;

import com.sofurry.model.IHasThumbnail;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.ICanHandleFeedback;
import com.sofurry.requests.ProgressSignal;
import com.sofurry.requests.RequestHandler;
import com.sofurry.requests.ThumbnailDownloaderThread;
import com.sofurry.util.ErrorHandler;


/**
 * @author Rangarig
 *
 * Yet another attempt to standartize the Gallery And the List Activity.
 */
public class ActivityManager<T> implements ICanHandleFeedback,ICanCancel {
	
	private ProgressBarHelper progh = null; 

	protected int viewSource = AppConstants.VIEWSOURCE_ALL; // The currently selected viewsource
	protected String viewSearch = "";						// The currently selected view Search
	protected int currentPage = 0;							// The currently selected page
	protected ArrayList<T> resultList;
	//protected ArrayList<String> pageIDs;					// The page ids
	
	protected ThumbnailDownloaderThread thumbnailDownloaderThread;
	private boolean currentlyFetching = false;
	
	private IManagedActivity<T> myAct = null;
	
	public ActivityManager(IManagedActivity<T> myAct) {
		setActivity(myAct);
	}
	
	/**
	 * Sets the activity this view is parented to
	 * @param myAct
	 */
	public void setActivity(IManagedActivity<T> myAct) {
		this.myAct = myAct;
		progh = new ProgressBarHelper(getAct(),this);
		requesthandler = new RequestHandler(this);
	}
	
	/**
	 * Return the activity without the interface
	 * @return
	 */
	private Activity getAct() {
		return (Activity)myAct;
	}
	
	public int getViewSource() {
		return viewSource;
	}

	public void setViewSource(int viewSource) {
		this.viewSource = viewSource;
	}

	public String getViewSearch() {
		return viewSearch;
	}

	public void setViewSearch(String viewSearch) {
		this.viewSearch = viewSearch;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public boolean isCurrentlyFetching() {
		return currentlyFetching;
	}

	public void setCurrentlyFetching(boolean currentlyFetching) {
		this.currentlyFetching = currentlyFetching;
	}

	public RequestHandler getRequesthandler() {
		return requesthandler;
	}
	
	public ArrayList<T> getResultList() {
		return resultList;
	}

	public void setResultList(ArrayList<T> resultList) {
		this.resultList = resultList;
	}
	
//	public ArrayList<String> getPageIDs() {
//		return pageIDs;
//	}
//
//	public void setPageIDs(ArrayList<String> pageIDs) {
//		this.pageIDs = pageIDs;
//	}

	/**
	 * Is called after the Activity Initialization is finished
	 */
	public void onActCreate() {
	    resultList = new ArrayList<T>();
	    
	    myAct.setUniqueKey(System.currentTimeMillis());
	    
	    Bundle extras = getAct().getIntent().getExtras();
	    if (extras != null) {
	    	viewSource = extras.getInt("viewSource");
	    	viewSearch = extras.getString("viewSearch");
	    }
	    //pageIDs = new ArrayList<String>();
	    myAct.plugInAdapter();
	    
		loadPage(true);
	}

	
	/**
	 * Used for Unique ID storage
	 * @param savedInstanceState
	 */
	public static void onCreateRefresh(IManagedActivity act, Bundle savedInstanceState) {
		if (savedInstanceState != null)
		  act.setUniqueKey(savedInstanceState.getLong("unique"));
	}
	
	
	
	/**
	 * Used for Unique ID storage
	 * @param outState
	 */
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("unique", myAct.getUniqueKey());
	}

	
	/**
	 * Called when a scrolling event occurs
	 * @param view
	 * @param first
	 * @param visible
	 * @param total
	 */
	public void onScroll(final AbsListView view, final int first, final int visible, final int total) {
		// detect if last item is visible
		if (currentlyFetching) return; // We will not make that request twice, if new data is already being fetched
		if (visible < total && (first + visible == total)) {// && listRequestThread == null) {
			Log.d("OnScrollListener - end of list", "fvi: " + first + ", vic: " + visible + ", tic: " + total);
			currentPage++;
			loadPage(false);
		}
	}

	
	/**
	 * The request handler to be used to handle the feedback from the AjaxRequest
	 */
	protected RequestHandler requesthandler = null;
	
	/* (non-Javadoc)
	 * @see com.sofurry.requests.CanHandleFeedback#onError(int, java.lang.Exception)
	 */
	public void onError(int id, Exception e) {
		if (id == AppConstants.REQUEST_ID_FETCHDATA) currentlyFetching = false; // Fetching failed, give the user a chance to try again
		hideProgressDialog();
		ErrorHandler.showError(getAct(), e);
	}

	/* (non-Javadoc)
	 * @see com.sofurry.requests.CanHandleFeedback#onData(int, org.json.JSONObject)
	 * Handles data send back from the feedback handler
	 */
	public void onData(int id, JSONObject obj) {
		if (id == AppConstants.REQUEST_ID_FETCHDATA) {
			currentlyFetching = false; // Fetching was successful, new pages may be fetched
			// Interpret the feedback data
			try {
				myAct.parseResponse(obj);
			} catch (Exception e) {
				onError(id, e);
			}
			Log.d("ONDATA", "OnData Received" + resultList.size());
			// Reset the adapter, so new entries are shown
			myAct.plugInAdapter();
		}
	    //myAct.updateView();
		hideProgressDialog();
		
	}

	public void onProgress(int id, ProgressSignal prg) {
	}

	/* (non-Javadoc)
	 * @see com.sofurry.requests.CanHandleFeedback#refresh()
	 * 
	 * Handles refresh Requests
	 */
	public void refresh() {
		myAct.updateView();
		hideProgressDialog();
	}

	public void onOther(int id, Object obj) throws Exception {
	}

	/**
	 * Creates a menu that can be used for all kinds of browsable lists. Used for Gallerys and lists alike here.
	 * @param menu
	 */
	public void createBrowsableMenu(Menu menu) {
		SubMenu viewSourceMenu = menu.addSubMenu("Filter & Search").setIcon(android.R.drawable.ic_menu_search);
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_KEYWORDS, 0, "Keywords");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_ALL, 0, "All Submissions");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_FEATURED, 0, "Featured");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_FAVORITES, 0, "Your Favorites");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_WATCHLIST, 0, "Watchlist");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_GROUP, 0, "Your Groups");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_WATCHLIST_COMBINED, 0, "Watches + Groups");
		//menu.add(0, AppConstants.MENU_FILTER_KEYWORDS, 0, "Keywords");
	}
	
	/**
	 * Handles menu events that arrive at the activity
	 * @param item
	 * The item that was selected
	 * @return
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_FILTER_KEYWORDS:
			Intent intent = new Intent(getAct(), TagEditor.class);
			getAct().startActivityForResult(intent, AppConstants.ACTIVITY_TAGS);
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
	
	/**
	 * Is forwarded from the activity, to handle callbacks of the TagEditor
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @return
	 */
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		// Handles the return value from TAGEditor
		if (requestCode == AppConstants.ACTIVITY_TAGS) {
			if (data == null) return true;
			viewSearch = data.getStringExtra("tags");
			resetViewSource(AppConstants.VIEWSOURCE_SEARCH);
			return true;
		}
		return false;
	}
	
	/**
	 * Loads the next page of browse results
	 * @param page
	 * The page to be loaded
	 * @param source
	 * The source
	 * @param showLoadingScreen
	 * if true, a loading screen is shown
	 */
	protected void loadPage(boolean showLoadingScreen) {
		stopThumbDownloader();
		if (showLoadingScreen)
			showProgressDialog("Fetching data...");
		else
			Toast.makeText(getAct().getApplicationContext(), "Fetching next page", Toast.LENGTH_SHORT).show();
		
		AjaxRequest request = myAct.getFetchParameters(currentPage, viewSource);
		request.setRequestID(AppConstants.REQUEST_ID_FETCHDATA);
		currentlyFetching = true;
		request.execute(requesthandler);		
	}


	/**
	 * Stops the thumbnail downloader Thread
	 */
	public void stopThumbDownloader() {
		if (thumbnailDownloaderThread != null) {
			thumbnailDownloaderThread.stopThread();
			thumbnailDownloaderThread = null;
		}
	}
	
	/**
	 * Starts the ThumbnailDownloader
	 */
	public void startThumbnailDownloader() {
		stopThumbDownloader();
		thumbnailDownloaderThread = new ThumbnailDownloaderThread(requesthandler, (ArrayList<IHasThumbnail>)resultList.clone());
		thumbnailDownloaderThread.start();
	}
	
	/**
	 * Resets the viewsource, when filter options are changed via menu.
	 * @param newViewSource
	 */
	public void resetViewSource(int newViewSource) {
		Log.i("SF", "ResetViewSource: "+newViewSource);
		viewSource = newViewSource;
		currentPage = 0;
		resultList = new ArrayList<T>();
	    //pageIDs = new ArrayList<String>();
		loadPage(true);
		myAct.resetViewSourceExtra(newViewSource);
	}
	
	/**
	 * Forwarded from Activity, closes the list when selected
	 */
	public void closeList() {
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		getAct().setResult(Activity.RESULT_OK, mIntent);
		getAct().finish();
	}

	/**
	 * Shows the progress Dialog
	 * @param msg
	 */
	public void showProgressDialog(String msg) {
		progh.showProgressDialog(msg);
	}
	
	/**
	 * Hides the progress Dialog
	 */
	public void hideProgressDialog() {
		progh.hideProgressDialog();
	}

	public void cancel() {
		requesthandler.killThreads(); // We instruct all running threads to terminate
		myAct.finish(); // We instruct the Activity to close
	}
	
	


	
}
