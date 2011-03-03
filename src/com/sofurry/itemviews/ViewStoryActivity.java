package com.sofurry.itemviews;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.base.classes.FavableActivity;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.FileStorage;

/**
 * @author SoFurry
 *
 * Activity that shows a single story
 */
public class ViewStoryActivity extends FavableActivity  {

	private String content;
	
	private WebView webview;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
	    webview = new WebView(this);
	    setContentView(webview);
	        
	    if (savedInstanceState == null) {
			AjaxRequest req = getFetchParameters(pageID);
			pbh.showProgressDialog("Fetching story...");
			req.execute(requesthandler);
	    } else {
	    	content = (String) retrieveObject("content");
			content = content.replace("\u00a0", "");
	    	showContent();
	    }
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		storeObject("content", content);
		super.onSaveInstanceState(outState);
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
		req.setRequestID(AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA);
		return req;
	}

	
	@Override
	public void onData(int id, JSONObject obj) throws Exception {
		pbh.hideProgressDialog();
		if (id == AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA) {
			content = obj.getString("content");
			content = content.replace("\u00a0", "");
			showContent();
		} else
			super.onData(id, obj); // Handle inherited events
	}

	/**
	 * Displays the content
	 */
	public void showContent() {
		webview.loadData(content, "text/html", "utf-8");
	}

	@Override
	public void createExtraMenuOptions(Menu menu) {
		super.createExtraMenuOptions(menu);
	}

	@Override
	public void save() {
		try {
			String targetPath = FileStorage.getUserStoragePath("Stories", sanitizeFileName(name) + ".html");

			File tf = new File(targetPath);
			FileStorage.ensureDirectory(tf.getParent());
			
			FileWriter outFile = new FileWriter(targetPath);
			PrintWriter out = new PrintWriter(outFile);
			out.println(content);
			out.close();
			
			Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			onError(-1, e);
		}
	}

}
