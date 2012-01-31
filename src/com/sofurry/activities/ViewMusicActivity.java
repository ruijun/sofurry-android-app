package com.sofurry.activities;

import java.io.File;

import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.base.classes.FavableActivity;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.mobileapi.core.RequestException;
import com.sofurry.mobileapi.downloaders.AsyncFileDownloader;
import com.sofurry.requests.AndroidDownloadWrapper;
import com.sofurry.requests.AndroidRequestWrapper;
import com.sofurry.requests.DataCall;
import com.sofurry.storage.FileStorage;

/**
 * @author Rangarig
 *
 * Allows to download and play music
 */
public class ViewMusicActivity extends FavableActivity  {

	// Variables to store, in case of orientation change
	private String playPath = null;
	private String filename = null;
	private String url = null;
	private String content = null;
	private boolean notwice = false;
	private AndroidDownloadWrapper down = null; // Placeholder for the downloader thread if it would be used.
	private DownMode downloadandplaymode = DownMode.play;
	
	private WebView webview;
	private Button buttondownload;
	
	enum DownMode {play, save};
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.musicdetails);
		buttondownload = (Button) findViewById(R.id.MusicDownloadAndPlay);
		
		buttondownload.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				downloadAndDo(DownMode.play);
			}});


	    webview = (WebView) findViewById(R.id.musictext);

	    if (savedInstanceState == null) {
			pbh.showProgressDialog("Fetching desc...");

			Request req = ApiFactory.createGetSubmissionData(pageID);
    		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
    		arw.exec(new DataCall() { public void call() { handleGetSubmissionData((JSONObject)arg1);	} });

	    } else {
	    	try {
		    	content = (String) retrieveObject("content");
		    	playPath = (String) retrieveObject("playPath");
		    	filename = (String) retrieveObject("filename");
		    	url = (String) retrieveObject("url");
		    	notwice = (Boolean) retrieveObject("notwice");
		    	down = (AndroidDownloadWrapper) retrieveObject("down");
		    	if (down!=null) initializeADWFeedback(); // Redirect feedback messages
		    	//downloadandplaymode = (Integer) retrieveObject("downloadandplaymode");
		    	String tmp = (String) retrieveObject("downloadandplaymode");
		    	downloadandplaymode = DownMode.valueOf(tmp);
		    	showContent();
		    	
		    	if (down != null) pbh.showProgressDialog("Downloading Song...");
			} catch (Exception e) {
				onError(e);
			}
	    }
	}
	
	/**
	 * Handles the return values from the getPageContent request
	 * @param obj
	 */
	public void handleGetSubmissionData(JSONObject obj) {
		try {
		  pbh.hideProgressDialog();
		  if (obj.has("description"))
		    content = obj.getString("description");
		  else 
			content = "-";
		  filename = obj.getString("fileName");
		  url =  obj.getString("contentSourceUrl");

		  showContent();
		} catch (Exception e) {
		  onError(e);
		}
	}
	
	/**
	 * Shows the content in the webview :)
	 */
	public void showContent() {
		  webview.loadData(content, "text/html", "utf-8");
	}
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		storeObject("content", content);
		storeObject("playPath", playPath);
		storeObject("filename", filename);
		storeObject("url", filename);
		storeObject("notwice", notwice);
		storeObject("down", down);
		storeObject("downloadandplaymode", downloadandplaymode.name());
		//storeObject("handler", requesthandler);

		super.onSaveInstanceState(outState);
	}




//	/**
//	 * Creates a request for submission data
//	 * @return
//	 */
//	public static AjaxRequest getFilenameRequest(int pid) {
//		AjaxRequest req = new AjaxRequest(AppConstants.SITE_URL + "/page/" + pid);
//		req.setRequestID(AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA);
//		return req;
//	}
	
	/**
	 * Downloads the music file, and replays it
	 */
	public void downloadAndDo(DownMode mode) {
		if (notwice) return;
		notwice = true;
		
		downloadandplaymode = mode;
		
		pbh.showProgressDialog("Fetching song data...");
		
		//Request req = ApiFactory.createGetSubmissionData(pageID);
		//AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
		//arw.exec(new DataCall() { public void call() { downloadFile((JSONObject)arg1);	} });
		downloadFile();
	}
	
	@Override
	public void createExtraMenuOptions(Menu menu) {
		menu.add(0,AppConstants.MENU_PLAY   ,0,"Play").setIcon(android.R.drawable.ic_media_play);
		super.createExtraMenuOptions(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_PLAY:
			downloadAndDo(DownMode.play);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Extracts the file URL from the fetched data, and downloads the file
	 * @param obj
	 */
	public void downloadFile()  {
		try {
			
			
			playPath = FileStorage.getPathRoot() + FileStorage.MUSIC_PATH;
			FileStorage.ensureDirectory(playPath);
			playPath += filename;
			
			File f = new File(playPath);
			if (f.exists()) {
				handlemusic();
				return;
			}
			
			// AppConstants._SongURLPrefix +
			pbh.hideProgressDialog();
			pbh.showProgressDialog("Downloading Song...");
			
			// Prepare async download
			down = new AndroidDownloadWrapper(requesthandler, url, playPath);
			initializeADWFeedback();
			down.start(); // Starts the download
		} catch (Exception e) {
			onError(e);
		}
	}
	
	/**
	 * Sets the callbacks to be used for the download feedback
	 */
	private void initializeADWFeedback() {
		down.setFinishFeedback(new DataCall() {
			public void call() throws Exception {
				handlemusic();
			}
		});
		down.setPercentageFeedback(new DataCall() {
			public void call() throws Exception {
				int i = (Integer)arg1;
				showProgress(i);
			}
		});
	}
	
	/**
	 * Handles progress messages from the AsyncDownloadManager
	 * @param i
	 */
	public void showProgress(int i) {
		try {
			pbh.setMessage("Downloaded " + i / 1024 + " kbyte...");
		} catch (Exception e) {
			onError(e);
		}
	}
	
	/**
	 * Decides what to do with the music, now that it is available
	 */
	public void handlemusic() {
		try {
			pbh.hideProgressDialog();
			down = null; // Destroy the downloader since, its no longer referenced
			if (downloadandplaymode == DownMode.play)
			  playmusic();
			if (downloadandplaymode == DownMode.save)
			  save();
		} catch (Exception e) {
			onError(e);
		}
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
           onError(e);
        } 	
		notwice = false;
	}

	
	@Override
	public void save() {
		if (playPath == null) {
			downloadAndDo(DownMode.save); // If the file has not been downloaded yet, download will be triggered here
			return;
		}
		try {
			File f = new File(playPath);
			if (!f.exists()) throw new Exception("File does not exist, try to download again.");
			
			String targetPath = FileStorage.getUserStoragePath("Music", FileStorage.sanitizeFileName(filename));

			File tf = new File(targetPath);
			FileStorage.ensureDirectory(tf.getParent());
			FileStorage.copyFile(f, tf);
			
			Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			onError(e);
		}
	}

	@Override
	public void onError(Exception e) {
		if (e instanceof RequestException)
		  notwice = false; // Just in case
		super.onError(e);
	}

	/* (non-Javadoc)
	 * @see com.sofurry.base.classes.SubmissionViewActivity#finish()
	 */
	@Override
	public void finish() {
		if (notwice) {
			try {
				down.cancel(); // Cancels the running download
			} catch (Exception e) {
			}
		}
		super.finish();
	}
	
	
	
	
	
	

}
