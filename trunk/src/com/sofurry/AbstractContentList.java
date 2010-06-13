package com.sofurry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public abstract class AbstractContentList<T> extends ListActivity implements Runnable {

	private String requestUrl;
	private Map<String, String> requestParameters;
	private Map<String, String> originalRequestParameters;
	private ProgressDialog pd;
	private int numResults;
	private ArrayList<T> resultList;
	private String errorMessage;

	// Get parameters and initiate data fetch thread
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestUrl = getFetchUrl();
		requestParameters = getFetchParameters();
		if (useAuthentication()) {
			// Save request parameters in case we have to re-send the request
			originalRequestParameters = new HashMap<String, String>(requestParameters);
			// add authentication parameters to the request
			requestParameters = Authentication.addAuthParametersToQuery(requestParameters);
		}
		pd = ProgressDialog.show(this, "Fetching data...", "Please wait", true, false);
		errorMessage = null;
		Thread thread = new Thread(this);
		thread.start();
	}

	// Asynchronous http request and result parsing
	public void run() {
		try {
			HttpResponse response = HttpRequest.doPost(requestUrl, requestParameters);
			String httpResult = EntityUtils.toString(response.getEntity());
			numResults = 0;
			resultList = new ArrayList<T>();
			try {
				if (useAuthentication() && Authentication.parseResponse(httpResult) == false) {
					// Retry request with new otp sequence if it failed for the first time
					requestParameters = Authentication.addAuthParametersToQuery(originalRequestParameters);
					response = HttpRequest.doPost(requestUrl, requestParameters);
					httpResult = EntityUtils.toString(response.getEntity());
				}
				errorMessage = parseErrorMessage(httpResult);
				if (errorMessage == null) {
					numResults = parseResponse(httpResult, resultList);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		handler.sendEmptyMessage(0);

	}

	// Separate handler to let android update the view whenever possible
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
			updateView();
			if (errorMessage != null) {
				closeList();
			}
		}
	};

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
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, (String[]) resultList
				.toArray(new String[numResults])));
		getListView().setTextFilterEnabled(true);
		  // bind a selection listener to the view
		  getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    public void onItemClick(AdapterView parentView, View childView, int position, long id) {
		      setSelectedIndex(position);
		    }
		  });

	}

	protected String parseErrorMessage(String httpResult) {
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

	protected abstract int parseResponse(String httpResult, ArrayList<T> list) throws JSONException;

	protected String getFetchUrl() {
		return AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
	}

	protected abstract void setSelectedIndex(int selectedIndex);
	
	protected abstract Map<String, String> getFetchParameters();

	protected abstract boolean useAuthentication();

}
