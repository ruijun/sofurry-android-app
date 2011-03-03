package com.sofurry.itemviews;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.SubmissionViewActivity;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.FileStorage;

/**
 * @author SoFurry
 *
 * Activity that shows a single story
 */
public class ViewJournalActivity extends SubmissionViewActivity  {
	
	private WebView webview;
	private String content;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
	    webview = new WebView(this);
	    setContentView(webview);
	    
	    if (savedInstanceState == null) {
			pbh.showProgressDialog("Fetching journal...");
			AjaxRequest req = getFetchParameters(pageID);
			req.execute(requesthandler);
	    } else {
			content = (String) retrieveObject("content");
			content = content.replace("\u00a0", "");
			viewContent();
	    }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		storeObject("content", content);
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
		req.setRequestID(AppConstants.REQUEST_ID_FETCHCONTENT);
		return req;
	}

	
	@Override
	public void onData(int id, JSONObject obj) throws Exception {
		if (id == AppConstants.REQUEST_ID_FETCHCONTENT) {
			pbh.hideProgressDialog();
			content = obj.getString("content");
			content = content.replace("\u00a0", "");
			viewContent();
		} else
			super.onData(id, obj);// Handle inherited events
	}
	
	public void viewContent() {
		webview.loadData(content, "text/html", "utf-8");
	}

	@Override
	public void createExtraMenuOptions(Menu menu) {
	}

	@Override
	public void save() {
		try {
			String targetPath = FileStorage.getUserStoragePath("Journals", sanitizeFileName(name) + ".html");

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
