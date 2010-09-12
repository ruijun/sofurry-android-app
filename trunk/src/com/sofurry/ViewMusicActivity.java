package com.sofurry;

import java.io.File;

import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.sofurry.model.Submission;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.AsyncFileDownloader;
import com.sofurry.util.ContentDownloader;
import com.sofurry.util.FileStorage;

/**
 * @author Rangarig
 *
 * Allows to download and play music
 */
public class ViewMusicActivity extends ActivityWithRequests  {
	
	private int pageID = 0;
	private String playPath = null;
	
	private WebView webview;
	private Button buttondownload;
	
	private AsyncFileDownloader down = null; // Placeholder for the downloader thread if it would be used.


	public void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.musicdetails);
		buttondownload = (Button) findViewById(R.id.MusicDownloadAndPlay);
		
		buttondownload.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				downloadAndPlay();
			}});


	    super.onCreate(savedInstanceState);	
	    webview = (WebView) findViewById(R.id.musictext);
//	    setContentView(webview);
	    Bundle extras = getIntent().getExtras() ;
	    if( extras != null ){
	        pageID = extras.getInt( "pageID" ) ;
//	        
			AjaxRequest req = getFetchParameters(pageID);
			pbh.showProgressDialog("Fetching desc...");
			req.execute(requesthandler);
	    }
	}
	
	/**
	 * Returns a story request
	 * @param pageID
	 * The pageID of the story to be fetched
	 * @return
	 */
	public static AjaxRequest getFetchParameters(int pageID) {
		AjaxRequest req = new AjaxRequest();
		req.setRequestID(AppConstants.REQUEST_ID_FETCHCONTENT);
		req.addParameter("f", "getpagecontent");
//		req.addParameter("f", "getdata");
		req.addParameter("pid", "" + pageID);
		return req;
	}

	/**
	 * Creates a request for submission data
	 * @return
	 */
	public static AjaxRequest getSubmissionData(int pid) {
		AjaxRequest req = new AjaxRequest();
		req.setRequestID(AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA);
		req.addParameter("f", "getpagecontent");
//		req.addParameter("f", "getdata");
		req.addParameter("pid", "" + pid);
		return req;
	}
	
	/**
	 * Downloads the music file, and replays it
	 */
	public void downloadAndPlay() {
		if (down != null) return; // We will not support nervous klickers
		
		AjaxRequest req = getSubmissionData(pageID);
		req.execute(requesthandler);
	}

	
	/**
	 * Extracts the file URL from the fetched data, and downloads the file
	 * @param obj
	 */
	public void downloadFile(JSONObject obj) throws Exception {
		String url = obj.getString("filename");
		
		//String tmp = sub.getFilenameUrl();
		File f = new File(url);
		
		playPath = FileStorage.getPath(f.getName());
	
		down = ContentDownloader.asyncDownload(url, f.getName(), requesthandler);
		pbh.showProgressDialog("Downloading song...");
	}
	
	/* (non-Javadoc)
	 * @see com.sofurry.ActivityWithRequests#sonOther(int, java.lang.Object)
	 * 
	 * Step Three 
	 */
	@Override
	public void sonOther(int id, Object obj) throws Exception {
		// Filedownload is finished, the the android play the file
		if (obj.getClass().equals(AsyncFileDownloader.class)) {
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW); 
	        intent.setDataAndType(Uri.parse(playPath),"audio/mp3"); 
	        try { 
	           startActivity(intent); 
	        } catch (ActivityNotFoundException e) { 
	           sonError(id, e);
	        } 			
			return;
		}
		super.sonOther(id, obj);
	}

	@Override
	public void sonData(int id, JSONObject obj) throws Exception {
		if (id == AppConstants.REQUEST_ID_FETCHCONTENT) {
		  String content = obj.getString("content");
		  webview.loadData(content, "text/html", "utf-8");
		}
		if (id == AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA) {
			downloadFile(obj);
		}
	}

}
