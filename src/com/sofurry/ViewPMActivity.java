package com.sofurry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.webkit.WebView;

import com.sofurry.requests.AjaxRequest;

public class ViewPMActivity extends ActivityWithRequests {

	private int PMID;
	private WebView webview;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webview = new WebView(this);
		setContentView(webview);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			PMID = extras.getInt("PMID");
			
			AjaxRequest req = getFetchParameters(PMID);
			pbh.showProgressDialog("Fetching data...");
			req.execute(requesthandler);
		}
	}

	protected AjaxRequest getFetchParameters(int id) {
		AjaxRequest req = new AjaxRequest();

		req.addParameter("f", "pmcontent");
		req.addParameter("id", "" + id);
		return req;
	}


	@Override
	public void sonData(int id, JSONObject obj) throws Exception {
		JSONArray items = new JSONArray(obj.getString("items"));
		JSONObject jsonItem = items.getJSONObject(0);
		String content = jsonItem.getString("message");
		webview.loadData(content, "text/html", "utf-8");
	}


}
