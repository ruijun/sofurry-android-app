package com.sofurry;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
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
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.sofurry.model.IHasThumbnail;
import com.sofurry.model.Submission;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.RequestHandler;
import com.sofurry.requests.ThumbnailDownloaderThread;
import com.sofurry.util.ErrorHandler;

/**
 * @author SoFurry
 *
 * Class that is used as a base for all GalleryViews
 *
 * @param <T>
 */
public abstract class AbstractContentGallery<T> extends Activity implements IContentActivity {

	private ProgressDialog pd;
	protected int numResults;
	protected ArrayList<T> resultList;
	protected ThumbnailDownloaderThread thumbnailDownloaderThread;
	private GridView galleryView;
	protected int currentPage = 0;
	protected int viewSource = AppConstants.VIEWSOURCE_ALL;
	protected String viewSearch = "";
	protected int lastScrollY = 0;

	// Get parameters and initiate data fetch thread
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallerylayout);
		galleryView = (GridView) findViewById(R.id.galleryview);
		loadPage(currentPage, viewSource, true);
	}
	
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
	
	/**
	 * Creates a menu that can be used for all kinds of browsable lists. Used for Gallerys and lists alike here.
	 * @param menu
	 */
	public static void createBrowsableMenu(Menu menu) {
		SubMenu viewSourceMenu = menu.addSubMenu("Filter");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_KEYWORDS, 0, "Keywords");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_ALL, 0, "All Submissions");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_FEATURED, 0, "Featured");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_FAVORITES, 0, "Your Favorites");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_WATCHLIST, 0, "Watchlist");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_GROUP, 0, "Your Groups");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_WATCHLIST_COMBINED, 0, "Watches + Groups");
		//menu.add(0, AppConstants.MENU_FILTER_KEYWORDS, 0, "Keywords");
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		createBrowsableMenu(menu);
		return result;
	}

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
			return super.onContextItemSelected(item);
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
	
	/**
	 * Terminates the thumbnail downloading thread
	 */
	public void stopThumbDownloader() {
		if (thumbnailDownloaderThread != null) {
			thumbnailDownloaderThread.stopThread();
			thumbnailDownloaderThread = null;
		}
	}
	
	/**
	 * Initializes the thumbnail downloading thread, for submissions
	 */
	public void startThumbnailDownloader() {
		stopThumbDownloader();
		thumbnailDownloaderThread = new ThumbnailDownloaderThread(requesthandler, (ArrayList<IHasThumbnail>)resultList);
		thumbnailDownloaderThread.start();
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
	protected void loadPage(int page, int source, boolean showLoadingScreen) {
		stopThumbDownloader();
		if (showLoadingScreen)
			pd = ProgressDialog.show(this, "Fetching data...", "Please wait", true, false);
		
		AjaxRequest request = getFetchParameters(page, source);
		request.execute(requesthandler);		
	}

	
	/**
	 * Closes this list and returns to the main menu
	 */
	private void closeList() {
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);
		finish();
	}

	/**
	 * Displays the list on screen
	 */
	private void updateView() {
		if (resultList == null)
			return;

		lastScrollY = galleryView.getFirstVisiblePosition();
		Log.i("SF AbstractContentList", "updateView called, last scrollpos: " + lastScrollY);
		//listRequestThread = null;
		BaseAdapter adapter = getAdapter(this);
		galleryView.setAdapter(adapter);
		// bind a selection listener to the view
		galleryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView parentView, View childView, int position, long id) {
				setSelectedIndex(position);
			}
		});
		galleryView.setOnScrollListener(new OnScrollListener() {
			public void onScroll(final AbsListView view, final int first, final int visible, final int total) {
				// detect if last item is visible
				if (visible < total && (first + visible == total)) {// && listRequestThread == null) {
					Log.d("OnScrollListener - end of list", "fvi: " + first + ", vic: " + visible + ", tic: " + total);
					currentPage++;
					loadPage(currentPage, viewSource, false);
				}
			}

			public void onScrollStateChanged(AbsListView view, int arg1) {
			}
		});
		Log.i("SF", "Scrolling TO: " + lastScrollY);
		//galleryView.setSelection(lastScrollY + 3);
	}

	public abstract void setSelectedIndex(int selectedIndex);

	public abstract AjaxRequest getFetchParameters(int page, int source);

	public abstract BaseAdapter getAdapter(Context context);

	public abstract void resetViewSource(int newViewSource);
	
	/**
	 * Parses the response from the Ajax interface
	 * @param obj
	 */
	public abstract void parseResponse(JSONObject obj);

	protected void updateContentList() {
		updateView();
	}
	
	//public Handler getHandler() {
	//	return handler;
	//}

	@Override
	public void finish() {
		super.finish();
		if (thumbnailDownloaderThread != null)
			thumbnailDownloaderThread.stopThread();
	}

}
