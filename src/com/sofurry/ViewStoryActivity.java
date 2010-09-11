package com.sofurry;

import org.json.JSONObject;

import android.os.Bundle;
import android.webkit.WebView;

import com.sofurry.requests.AjaxRequest;

public class ViewStoryActivity extends ActivityWithRequests  {
	
	private int pageID;
	private WebView webview;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
	    webview = new WebView(this);
	    setContentView(webview);
	    Bundle extras = getIntent().getExtras() ;
	    if( extras != null ){
	        pageID = extras.getInt( "pageID" ) ;
	        
			AjaxRequest req = getFetchParameters(pageID);
			pbh.showProgressDialog("Fetching story...");
			req.execute(requesthandler);
	    }
	}

	/**
	 * Returns a story request
	 * @param pageID
	 * The pageID of the story to be fetched
	 * @return
	 */
	protected AjaxRequest getFetchParameters(int pageID) {
		AjaxRequest req = new AjaxRequest();
		req.addParameter("f", "getpagecontent");
		req.addParameter("pid", "" + pageID);
		return req;
	}

	
	@Override
	public void sonData(int id, JSONObject obj) throws Exception {
		String content = obj.getString("content");
		webview.loadData(content, "text/html", "utf-8");
	}

}
