package com.sofurry;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.AbsListView.OnScrollListener;

import com.sofurry.model.Submission;

public abstract class AbstractContentGallery<T> extends Activity implements ContentController<T> {
	
	private String requestUrl;
	private Map<String, String> requestParameters;
	private ProgressDialog pd;
	protected int numResults;
	protected ArrayList<T> resultList;
	private String errorMessage;
	protected ThumbnailDownloaderThread thumbnailDownloaderThread;
	protected ContentRequestThread<Submission> listRequestThread;
	private GridView galleryView;
	protected int currentPage = 0;
	protected int lastScrollY = 0;

	// Separate handler to let android update the view whenever possible
	protected Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj != null) {
				resultList = (ArrayList<T>) msg.obj;
				pd.dismiss();
				updateView();
				if (errorMessage != null) {
					closeList();
				}
			} else {
				updateContentList();
			}
		}
	};

	// Get parameters and initiate data fetch thread
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallerylayout);
		galleryView = (GridView) findViewById(R.id.galleryview);
		pd = ProgressDialog.show(this, "Fetching data...", "Please wait", true, false);
		loadPage(currentPage);
	}
	
	protected void loadPage(int page) {
		if (thumbnailDownloaderThread != null) {
			thumbnailDownloaderThread.stopThread();
			thumbnailDownloaderThread = null;
		}
		requestUrl = getFetchUrl();
		requestParameters = getFetchParameters(page);
		errorMessage = null;
		listRequestThread = new ContentRequestThread(this, handler, requestUrl, requestParameters);
		listRequestThread.start();
	}

	// Goes back to the main menu
	private void closeList() {
		Bundle bundle = new Bundle();
		if (errorMessage != null) {
			bundle.putString("errorMessage", errorMessage);
		}
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);
		finish();
	}

	// Sets the resulting list on the screen
	private void updateView() {
		if (resultList == null)
			return;

		lastScrollY = galleryView.getFirstVisiblePosition();
		Log.i("SF AbstractContentList", "updateView called, last scrollpos: "+lastScrollY);
		listRequestThread = null;
		BaseAdapter adapter = getAdapter(this);
		galleryView.setAdapter(adapter);
		// bind a selection listener to the view
		galleryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView parentView, View childView, int position, long id) {
				setSelectedIndex(position);
			}
		});
	    galleryView.setOnScrollListener(new OnScrollListener() {
	        public void onScroll(final AbsListView view, final int first,
	                                    final int visible, final int total) {
	            // detect if last item is visible
	            if (visible < total && (first + visible == total) && listRequestThread == null) {
	                Log.d("OnScrollListener - end of list", "fvi: " +
	                   first + ", vic: " + visible + ", tic: " + total);
	                currentPage++;
	        		loadPage(currentPage);
	            }
	        }

			public void onScrollStateChanged(AbsListView view, int arg1) {
			}
	    }); 
		Log.i("SF", "Scrolling TO: "+lastScrollY);
	    galleryView.setSelection(lastScrollY+3);


	}

	public String parseErrorMessage(String httpResult) {
		try {
			// check for json error message and parse it
			Log.d("List.parseErrorMessage", "response: " + httpResult);
			JSONObject jsonParser;
			jsonParser = new JSONObject(httpResult);
			int messageType = jsonParser.getInt("messageType");
			if (messageType == AppConstants.AJAXTYPE_APIERROR) {
				String error = jsonParser.getString("error");
				Log.d("List.parseErrorMessage", "Error: " + error);
				return error;
			}
		} catch (JSONException e) {
			Log.d("Auth.parseResponse", e.toString());
		}

		return null;

	}

	protected String getFetchUrl() {
		return AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
	}

	protected abstract void setSelectedIndex(int selectedIndex);

	protected abstract Map<String, String> getFetchParameters(int page);

	protected abstract BaseAdapter getAdapter(Context context);

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
