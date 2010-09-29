package com.sofurry.itemviews;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.webkit.WebView;

import com.sofurry.ActivityWithRequests;
import com.sofurry.AppConstants;
import com.sofurry.requests.AjaxRequest;

public class ViewPMActivity extends ActivityWithRequests {

	private String content = null;
	private int PMID;
	private WebView webview;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webview = new WebView(this);
		setContentView(webview);
		
		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				PMID = extras.getInt("PMID");
				
				AjaxRequest req = getFetchParameters(PMID);
				pbh.showProgressDialog("Fetching data...");
				req.execute(requesthandler);
			}
		} else {
			content = (String) retrieveObject("content");
			showContent();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		storeObject("content", content);
	}

	protected AjaxRequest getFetchParameters(int id) {
		AjaxRequest req = new AjaxRequest();
		req.addParameter("f", "pmcontent");
		req.addParameter("id", "" + id);
		req.setRequestID(AppConstants.REQUEST_ID_FETCHCONTENT);
		return req;
	}

	@Override
	public void onData(int id, JSONObject obj) throws Exception {
		if (id == AppConstants.REQUEST_ID_FETCHCONTENT) {
			pbh.hideProgressDialog();
			JSONArray items = new JSONArray(obj.getString("items"));
			JSONObject jsonItem = items.getJSONObject(0);
			content = jsonItem.getString("message");
			showContent();
		} else 
			super.onData(id, obj);// Handle inherited events
	}
	
	/**
	 * Shows the content
	 */
	public void showContent() {
		webview.loadData(content, "text/html", "utf-8");
	}


}
