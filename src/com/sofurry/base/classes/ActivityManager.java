package com.sofurry.base.classes;

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

import com.sofurry.AppConstants;
import com.sofurry.activities.TagEditorActivity;
import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.ICanHandleFeedback;
import com.sofurry.base.interfaces.IHasThumbnail;
import com.sofurry.base.interfaces.IManagedActivity;
import com.sofurry.helpers.ProgressBarHelper;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.requests.AndroidRequestWrapper;
import com.sofurry.requests.DataCall;
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

	protected ViewSource viewSource = ViewSource.all; // The currently selected viewsource
	protected String viewSearch = "";						// The currently selected view Search
	protected int currentPage = 0;							// The currently selected page
	public int totalPages = 0;							// total number of available pages. stop load after last page. 0 = unknown amount of pages
	protected String currentTitle = "";						// Title for current page
	protected ArrayList<T> resultList;
	
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
	
	public ViewSource getViewSource() {
		return viewSource;
	}

	public void setViewSource(ViewSource viewSource) {
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
	
	/**
	 * Returns the contenttype the Activity is showing
	 * @return the contentType
	 */
	public ContentType getContentType() {
		return myAct.getContentType();
	}

	/**
	 * Is called after the Activity Initialization is finished
	 */
	public void onActCreate() {
	    resultList = new ArrayList<T>();
	    
	    myAct.setUniqueKey(System.currentTimeMillis());
	    
	    Bundle extras = getAct().getIntent().getExtras();
	    if (extras != null) {
	    	viewSource = ViewSource.valueOf(extras.getString("viewSource"));
	    	viewSearch = extras.getString("viewSearch");
	    	currentTitle = extras.getString("activityTitle");
/*	    	if (currentTitle.length() > 0) {
	    		((Activity) myAct).setTitle(currentTitle);
	    	} /**/
	    }
	    //pageIDs = new ArrayList<String>();
	    myAct.plugInAdapter();
	    
		loadPage(true);
	}
	
	
	/**
	 * Creates the Fetch request for the attached activity
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public Request getFetchRequest(int page) throws Exception {
		String search = null;
		if (!"".equals(getViewSearch()))
			search = getViewSearch();
		Request req = ApiFactory.createBrowse(getViewSource(),search,getContentType(),AppConstants.ENTRIESPERPAGE_GALLERY,page);
		return req;
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
		if (visible < total && (first + visible == total) && ((totalPages <= 0) || (currentPage < totalPages-1))) {// && listRequestThread == null) {
			Log.d(AppConstants.TAG_STRING, "onScrollListener - End of list: fvi: " + first + ", vic: " + visible + ", tic: " + total);
			currentPage++;
			loadPage(false);
		}
	}

	public void forceLoadNext() {
		if (currentlyFetching) return; // We will not make that request twice, if new data is already being fetched
		if ((totalPages > 0) && (currentPage >= totalPages-1)) return; // don't fetch after last page
		currentPage++;
		loadPage(false);
	}
	
	/**
	 * The request handler to be used to handle the feedback from the AjaxRequest
	 */
	protected RequestHandler requesthandler = null;

	
	public void onError(Exception e) {
		currentlyFetching = false; // Fetching failed, give the user a chance to try again
		hideProgressDialog();
		ErrorHandler.showError(getAct(), e);
	}
	
	public void handleData(JSONObject obj) {
		currentlyFetching = false; // Fetching was successful, new pages may be fetched
		// Interpret the feedback data
		try {
			myAct.parseResponse(obj);
		} catch (Exception e) {
			onError(e);
		}
		Log.d(AppConstants.TAG_STRING, "OnData Received" + resultList.size());
		// Reset the adapter, so new entries are shown
		myAct.plugInAdapter();
		hideProgressDialog();
	}

//	public void onProgress(int id, ProgressSignal prg) {
//	}

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
			Intent intent = new Intent(getAct(), TagEditorActivity.class);
			getAct().startActivityForResult(intent, AppConstants.ACTIVITY_TAGS);
			return true;
		case AppConstants.MENU_FILTER_ALL:
			currentTitle = "Recent";
			resetViewSource(ViewSource.all);
			return true;
		case AppConstants.MENU_FILTER_FEATURED:
			currentTitle = "Featured";
			resetViewSource(ViewSource.featured);
			return true;
		case AppConstants.MENU_FILTER_FAVORITES:
			currentTitle = "Favorites";
			resetViewSource(ViewSource.favorites);
			return true;
		case AppConstants.MENU_FILTER_WATCHLIST:
			currentTitle = "Watchlist";
			resetViewSource(ViewSource.watchlist);
			return true;
		case AppConstants.MENU_FILTER_GROUP:
			currentTitle = "Group";
			resetViewSource(ViewSource.group);
			return true;
		case AppConstants.MENU_FILTER_WATCHLIST_COMBINED:
			currentTitle = "Combined";
			resetViewSource(ViewSource.watchlist_combined);
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
			currentTitle = "Tags";
			resetViewSource(viewSource.search);
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
		
		//ToDo myAct is not always activity. Replace this hack with clean code to set activity title 
		if (currentTitle.length() > 0) {
			((Activity) myAct).setTitle(currentTitle);
		}

		try {
			// The method getFetch request will per default relay this call to the localGetFetchRequest routine,
			// it may however be overriden by the implemented activity class. (Currently used in ListPM activity)
			Request request = myAct.getFetchRequest(currentPage);
			// Okay, this is a little bit of excessive anonymous classing, but I am sure it could be worse!
			// However since I am going to look back at this and will think, WTF did I smoke when I... and so on.
			//
			// The request will call back from the worker thread. So its not enough to encapsulate the two method calls
			// in the callback object. We need to have the method call from inside of the guithread.
			// Therefore we encapsulate the actual message call in the DataCall object and feed that
			// into the request handler, so the handler will make the actual call.
			// So we have the anonymous callback class, using two anonymous DataCall classes.
			// Well... I have the feeling there should be an easier way to accomplish that,
			// but with the goal in mind that the MobileApi should not be android specific, I do
			// not see how.
			AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, request);
			arw.exec(new DataCall() {
				@Override
				public void call() {
					handleData((JSONObject)arg1);
				}
			});
			
// Old version of this request, much of this is handled by the wrapper now.			
//			request.executeAsync(
//					new CallBack(){ 
//						public void success(JSONObject result){
//							Message msg = new Message();
//							msg.obj = new DataCall(result) {
//								public void call() {
//									handleData((JSONObject)arg1);
//								}
//							};
//							getRequesthandler().postMessage(msg);
//						}; 
//						public void fail(Exception e){
//							Message msg = new Message();
//							msg.obj = new DataCall(e) {
//								public void call() {
//									onError((Exception)arg1);
//								}
//							};
//							getRequesthandler().postMessage(msg);
//						}; 
//					} 
//			);
			currentlyFetching = true;
		} catch (Exception e) {
			onError(e);
		}
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
	public void resetViewSource(ViewSource newViewSource) {
		Log.i(AppConstants.TAG_STRING, "ResetViewSource: "+newViewSource);
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
