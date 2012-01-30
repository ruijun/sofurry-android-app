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
	private String content = null;
	private boolean notwice = false;
	private AsyncFileDownloader down = null; // Placeholder for the downloader thread if it would be used.
	private int downloadandplaymode = 1;
	
	private WebView webview;
	private Button buttondownload;
	
	private static int downmode_play = 1;
	private static int downmode_save = 2;
	

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.musicdetails);
		buttondownload = (Button) findViewById(R.id.MusicDownloadAndPlay);
		
		buttondownload.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				downloadAndDo(downmode_play);
			}});


	    webview = (WebView) findViewById(R.id.musictext);

	    if (savedInstanceState == null) {
			pbh.showProgressDialog("Fetching desc...");

			Request req = ApiFactory.createGetPageContent(pageID);
    		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
    		arw.exec(new DataCall() { public void call() { handleFetchContent((JSONObject)arg1);	} });

	    } else {
	    	try {
		    	content = (String) retrieveObject("content");
		    	playPath = (String) retrieveObject("playPath");
		    	filename = (String) retrieveObject("filename");
		    	notwice = (Boolean) retrieveObject("notwice");
		    	down = (AsyncFileDownloader) retrieveObject("down");
		    	downloadandplaymode = (Integer) retrieveObject("downloadandplaymode");
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
	public void handleFetchContent(JSONObject obj) {
		try {
		  pbh.hideProgressDialog();
		  content = obj.getString("content");
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
		storeObject("notwice", notwice);
		storeObject("down", down);
		storeObject("downloadandplaymode", downloadandplaymode);
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
	public void downloadAndDo(int mode) {
		if (notwice) return;
		notwice = true;
		downloadandplaymode = mode;
		
		pbh.showProgressDialog("Fetching song data...");
		
		Request req = ApiFactory.createGetSubmissionData(pageID);
		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
		arw.exec(new DataCall() { public void call() { downloadFile((JSONObject)arg1);	} });
	}
	
	@Override
	public void createExtraMenuOptions(Menu menu) {
		menu.add(0,AppConstants.MENU_PLAY   ,0,"Play").setIcon(android.R.drawable.ic_media_play);
		super.createExtraMenuOptions(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_PLAY:
			downloadAndDo(downmode_play);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Extracts the file URL from the fetched data, and downloads the file
	 * @param obj
	 */
	public void downloadFile(JSONObject submissionData)  {
		try {
			
			String fname = submissionData.getString("fileName");
			
			playPath = FileStorage.getPathRoot() + FileStorage.MUSIC_PATH;
			FileStorage.ensureDirectory(playPath);
			playPath += fname;
			
			File f = new File(playPath);
			if (f.exists()) {
				handlemusic();
				return;
			}
			
			String url = AppConstants._SongURLPrefix + submissionData.getString("contentSourceUrl");
			
			
			pbh.hideProgressDialog();
			pbh.showProgressDialog("Downloading Song...");
			
			// Prepare async download
			AndroidDownloadWrapper adw = new AndroidDownloadWrapper(requesthandler, url, playPath);
			adw.setFinishFeedback(new DataCall() {
				public void call() throws Exception {
					handlemusic();
				}
			});
			adw.setPercentageFeedback(new DataCall() {
				public void call() throws Exception {
					int i = (Integer)arg1;
					showProgress(i);
				}
			});
			adw.start(); // Starts the download
			
		} catch (Exception e) {
			onError(e);
		}
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
			if (downloadandplaymode == downmode_play)
			  playmusic();
			if (downloadandplaymode == downmode_save)
			  save();
		} catch (Exception e) {
			onError(e);
		}
	}
	
//	/**
//	 * Returns the filename of the mp3 file as it resides on device
//	 * @return
//	 */
//	public String getFname() {
//		return "/m" + pageID + ".mp3";
//	}
	
//	@Override
//	public void onProgress(int id, ProgressSignal prg) {
//		pbh.setMessage("Downloaded " + prg.progress / 1024 + " kbyte...");
//	}

//	/* (non-Javadoc)
//	 * @see com.sofurry.ActivityWithRequests#sonOther(int, java.lang.Object)
//	 * 
//	 * Step Three 
//	 */
//	@Override
//	public void onOther(int id, Object obj) throws Exception {
//		if (id == AppConstants.REQUEST_ID_FETCHSUBMISSIONDATA) {
//			String str = (String)obj;
//			downloadFile(str);
//			return; // Important :/
//		}
//		
//		// Filedownload is finished, the the android play the file
//		if (obj.getClass().equals(AsyncFileDownloader.class)) {
//			handlemusic();
//			return;
//		}
//		super.onOther(id, obj);
//	}
	
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
			downloadAndDo(downmode_save); // If the file has not been downloaded yet, download will be triggered here
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
	
	
	
	

}
