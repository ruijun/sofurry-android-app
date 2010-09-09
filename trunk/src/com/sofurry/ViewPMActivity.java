package com.sofurry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.sofurry.model.PrivateMessage;
import com.sofurry.util.Authentication;
import com.sofurry.util.HttpRequest;

public class ViewPMActivity extends Activity implements Runnable {

	private String requestUrl;
	private Map<String, String> requestParameters;
	private Map<String, String> originalRequestParameters;
	private ProgressDialog pd;
	private String errorMessage;
	private int PMID;
	private WebView webview;
	private PrivateMessage displayPrivateMessage;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webview = new WebView(this);
		setContentView(webview);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			PMID = extras.getInt("PMID");
			
			requestUrl = AppConstants.SITE_URL + AppConstants.SITE_REQUEST_SCRIPT;
			requestParameters = getFetchParameters(PMID);
			// Save request parameters in case we have to re-send the request
			originalRequestParameters = new HashMap<String, String>(requestParameters);
			// add authentication parameters to the request
			Authentication.addAuthParametersToQuery(requestParameters);
			pd = ProgressDialog.show(this, "Fetching data...", "Please wait", true, false);
			errorMessage = null;
			Thread thread = new Thread(this);
			thread.start();
		}
	}

	protected Map<String, String> getFetchParameters(int id) {
		Map<String, String> kvPairs = new HashMap<String, String>();

		kvPairs.put("f", "pmcontent");
		kvPairs.put("id", "" + id);
		return kvPairs;
	}

	// Asynchronous http request and result parsing
	public void run() {
		try {
			HttpResponse response = HttpRequest.doPost(requestUrl, requestParameters);
			String httpResult = EntityUtils.toString(response.getEntity());
			try {
				if (Authentication.parseResponse(httpResult) == false) {
					// Retry request with new otp sequence if it failed for the
					// first time
					Authentication.addAuthParametersToQuery(originalRequestParameters);
					response = HttpRequest.doPost(requestUrl, requestParameters);
					httpResult = EntityUtils.toString(response.getEntity());
				}
				errorMessage = parseErrorMessage(httpResult);
				if (errorMessage == null) {
					displayPrivateMessage = parseResponse(httpResult);
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

	private void updateView() {
		webview.loadData(displayPrivateMessage.getMessage(), "text/html", "utf-8");
	}

	// Goes back to the story list
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

	protected PrivateMessage parseResponse(java.lang.String httpResult) throws JSONException {
		PrivateMessage pm  = new PrivateMessage();
		Log.i("ViewPMActivity.parseResponse", "response: " + httpResult);

		//TODO: Support threaded view
		JSONObject jsonParser = new JSONObject(httpResult);
		JSONArray items = new JSONArray(jsonParser.getString("items"));
		JSONObject jsonItem = items.getJSONObject(0);
		String content = jsonItem.getString("message");
		pm.setMessage(content);
		return pm;
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

}
