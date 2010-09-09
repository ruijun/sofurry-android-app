package com.sofurry;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.AbsListView.OnScrollListener;

import com.sofurry.model.Submission;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.RequestHandler;

public abstract class AbstractContentList<T> extends ListActivity {

	//private String requestUrl;
	//private Map<String, String> requestParameters;
	private ProgressDialog pd;
	protected int numResults;
	protected ArrayList<T> resultList;
	private String errorMessage;
	protected ThumbnailDownloaderThread thumbnailDownloaderThread;
	protected ContentRequestThread<Submission> listRequestThread;
	protected int currentPage = 0;
	protected int viewSource = AppConstants.VIEWSOURCE_ALL;
	protected int lastScrollY = 0;

//	// Separate handler to let android update the view whenever possible
//	protected Handler handler = new Handler() {
//		@SuppressWarnings("unchecked")
//		@Override
//		public void handleMessage(Message msg) {
//			if (msg.obj != null) {
//				resultList = (ArrayList<T>) msg.obj;
//				pd.dismiss();
//				updateView();
//				if (errorMessage != null) {
//					closeList();
//				}
//			} else {
//				updateContentList();
//			}
//		}
//	};
	
	/**
	 * The request handler to be used to handle the feedback from the AjaxRequest
	 */
	protected RequestHandler requesthandler = new RequestHandler() {
		
		@Override
		public void onError(Exception e) {
			closeList();
			ronError(e);
		}
		
		@Override
		public void onData(JSONObject obj) {
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
		// TODO: Let the user know, what happened here.
		Log.e("Error", e.getMessage());
		
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
		SubMenu viewSourceMenu = menu.addSubMenu("Filter");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_ALL, 0, "All Submissions");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_FEATURED, 0, "Featured");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_FAVORITES, 0, "Your Favorites");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_WATCHLIST, 0, "Watchlist");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_GROUP, 0, "Your Groups");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_WATCHLIST_COMBINED, 0, "Watches + Groups");
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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

	public void loadPage(int pageNum, int source, boolean showLoadingScreen) {
		if (thumbnailDownloaderThread != null) {
			thumbnailDownloaderThread.stopThread();
			thumbnailDownloaderThread = null;
		}
		if (showLoadingScreen)
			pd = ProgressDialog.show(this, "Fetching data...", "Please wait", true, false);
		
		//requestUrl = getFetchUrl();
		//requestParameters = getFetchParameters(pageNum, source);
		AjaxRequest ar = new AjaxRequest(AppConstants.getFetchUrl(), getFetchParameters(pageNum, source));
		errorMessage = null; // TODO: Whats this for?
		ar.execute(requesthandler); // Requests the data, and will redirect results to this object
		//listRequestThread = new ContentRequestThread(this, requestUrl, requestParameters);
		//listRequestThread.start();
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
		listRequestThread = null;
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
	            if (visible < total && (first + visible == total) && listRequestThread == null) {
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

//	public String parseErrorMessage(String httpResult) {
//		try {
//			// check for json error message and parse it
//			Log.d("List.parseErrorMessage", "response: " + httpResult);
//			JSONObject jsonParser;
//			jsonParser = new JSONObject(httpResult);
//			int messageType = jsonParser.getInt("messageType");
//			if (messageType == AppConstants.AJAXTYPE_APIERROR) {
//				String error = jsonParser.getString("error");
//				Log.d("List.parseErrorMessage", "Error: " + error);
//				return error;
//			}
//		} catch (JSONException e) {
//			Log.d("Auth.parseResponse", e.toString());
//		}
//
//		return null;
//
//	}

	protected abstract void setSelectedIndex(int selectedIndex);

	protected abstract Map<String, String> getFetchParameters(int page, int source);

	protected abstract ListAdapter getAdapter(Context context);

	protected abstract void resetViewSource(int newViewSource);
	
	protected abstract void parseResponse(JSONObject obj);

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
