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
	private boolean notwice = false;
	
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
	        //String username = removeExtraChars(extras.getString( "username" )).toLowerCase();  
	        //String name = removeExtraChars(extras.getString( "name" )).toLowerCase();  
	        
	        // Well this is not partciulary neat, but since there is currently no other way to obtain the URL that I am aware of...
	        //fileurl = AppConstants.SITE_URL + "/art/music/" + username + "/" + username + "_" + name + ".mp3";
	        
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
		req.addParameter("pid", "" + pageID);
		return req;
	}

	/**
	 * Creates a request for submission data
	 * @return
	 */
	public static AjaxRequest getFilenameRequest(int pid) {
		AjaxRequest req = new AjaxRequest(AppConstants.SITE_URL + "/page/" + pid);
		req.setRequestID(AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA);
		return req;
	}
	
	/**
	 * Downloads the music file, and replays it
	 */
	public void downloadAndPlay() {
		if (notwice) return;
		notwice = true;
		
		pbh.showProgressDialog("Fetching song data...");
		AjaxRequest req = getFilenameRequest(pageID);
		req.execute(requesthandler);
//		downloadFile(fileurl);
	}

	/**
	 * Extracts the file URL from the fetched data, and downloads the file
	 * @param obj
	 */
	public void downloadFile(String html) throws Exception {
		//String url = obj.getString("filename");
		
		// Find markers
		int end = html.indexOf(AppConstants.MP3DownloadLinkEndMarker);
		int beg = end;
		int len = AppConstants.MP3DownloadLinkStartMarker.length();
		boolean found = false;
		// Okay, this might suck, but right now I cant remember the correct phrase. It will work though.
		while ((beg > 0) && (!found)) {
			beg--;
			if (html.substring(beg, beg + len).equals(AppConstants.MP3DownloadLinkStartMarker))
				found = true;
		}
		if (beg == 0) throw new Exception("URL Extract failed");
		
		beg += len;
		
		String tmp = html.substring(beg,end);

		String fname = "music" + pageID + ".mp3";
		playPath = FileStorage.getPath(fname);

		File f = new File(playPath);
		if (f.exists()) {
			playmusic();
			return;
		}
		
		pbh.showProgressDialog("Downloading song...");
		down = ContentDownloader.asyncDownload(tmp, fname, requesthandler);
	}
	
	/* (non-Javadoc)
	 * @see com.sofurry.ActivityWithRequests#sonOther(int, java.lang.Object)
	 * 
	 * Step Three 
	 */
	@Override
	public void sonOther(int id, Object obj) throws Exception {
		if (id == AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA) {
			String str = (String)obj;
			downloadFile(str);
			return; // Important :/
		}
		
		// Filedownload is finished, the the android play the file
		if (obj.getClass().equals(AsyncFileDownloader.class)) {
			playmusic();
			return;
		}
		super.sonOther(id, obj);
	}
	
	/**
	 * Plays the music file submitted
	 * @param path
	 */
	public void playmusic() {
		down = null;
		pbh.hideProgressDialog();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW); 
        intent.setDataAndType(Uri.parse("file:///" + playPath),"audio/mp3"); 
        try { 
           startActivity(intent); 
        } catch (ActivityNotFoundException e) { 
           sonError(-1, e);
        } 	
		notwice = false;
	}

	@Override
	public void sonData(int id, JSONObject obj) throws Exception {
		if (id == AppConstants.REQUEST_ID_FETCHCONTENT) {
		  String content = obj.getString("content");
		  webview.loadData(content, "text/html", "utf-8");
		}
//		if (id == AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA) {
//			downloadFile(obj);
//		}
	}


	@Override
	public void sonError(int id, Exception e) {
		down = null;
		pbh.hideProgressDialog();
		super.sonError(id, e);
	}
	
	

}
