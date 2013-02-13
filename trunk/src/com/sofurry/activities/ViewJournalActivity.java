package com.sofurry.activities;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Toast;

import com.sofurry.base.classes.SubmissionViewActivity;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.downloaders.ContentDownloader;
import com.sofurry.storage.FileStorage;

/**
 * @author SoFurry
 *
 * Activity that shows a single story
 */
public class ViewJournalActivity extends SubmissionViewActivity  {
	
	private WebView webview;
	private String content;
	private AsyncTask<String, Integer, String> downloader = null;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
	    
	    setTitle(name);
	    
	    webview = new WebView(this);
	    setContentView(webview);
	    
	    if (savedInstanceState == null) {
			pbh.showProgressDialog("Fetching journal...");
			
			
			//Request req = ApiFactory.createGetPageContent(pageID);
/*			Request req = ApiFactory.createGetSubmissionData(pageID);
    		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
    		arw.exec(new DataCall() { public void call() { handlePageContent((JSONObject)arg1);	} });/**/

			downloader = new AsyncTask<String, Integer, String>() {
				@Override
				protected String doInBackground(String... params) {
					try {
						return ContentDownloader.downloadText(params[0]);
					} catch (Exception e) {
						e.printStackTrace();
						return "";
					}
				}

				@Override
				protected void onPostExecute(String result) {
					pbh.hideProgressDialog();
					content = result;
					if (content.length() > 0)
						content = "<center><img src='"+ApiFactory.getUserIconURL(authorId)+"'></center><br>"+content;
					content = content.replace("\u00a0", "");
					viewContent();
				}
				
			};
			
			downloader.execute(ApiFactory.getFullURL(pageID));
					
			
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

//	/**
//	 * Returns a story request
//	 * @param pageID
//	 * The pageID of the story to be fetched
//	 * @return
//	 */
//	protected Request getFetchParameters(int pageID) {
//		AjaxRequest req = new AjaxRequest();
//		req.addParameter("f", "getpagecontent");
//		req.addParameter("pid", "" + pageID);
//		req.setRequestID(AppConstants.REQUEST_ID_FETCHCONTENT);
//		return req;
//	}

	
	/**
	 * Handles the feedback by the getPage Content
	 * @param obj
	 */
/*	public void handlePageContent(JSONObject obj)  {
		try {
			pbh.hideProgressDialog();
		    if (obj.has("description"))
			  content = obj.getString("description");
			else 
			  content = "-";
			content = content.replace("\u00a0", "");
			viewContent();
		} catch (Exception e) {
			onError(e);
		}
	}/**/
	
	public void viewContent() {
		webview.loadData(content, "text/html", "utf-8");
	}

	@Override
	public void createExtraMenuOptions(Menu menu) {
	}

	@Override
	public void save() {
		try {
			String targetPath = FileStorage.getUserStoragePath("Journals", FileStorage.sanitizeFileName(name) + ".html");

			File tf = new File(targetPath);
			FileStorage.ensureDirectory(tf.getParent());
			
			FileWriter outFile = new FileWriter(targetPath);
			PrintWriter out = new PrintWriter(outFile);
			out.println(content);
			out.close();
			
			Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			onError(e);
		}
	}

}
