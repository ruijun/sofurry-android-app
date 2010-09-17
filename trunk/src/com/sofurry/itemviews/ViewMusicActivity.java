package com.sofurry.itemviews;

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
import com.sofurry.FavableActivity;
import com.sofurry.R;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.AsyncFileDownloader;
import com.sofurry.util.ContentDownloader;
import com.sofurry.util.FileStorage;

/**
 * @author Rangarig
 *
 * Allows to download and play music
 */
public class ViewMusicActivity extends FavableActivity  {
	
	private String playPath = null;
	private String filename = null;
	
	private WebView webview;
	private Button buttondownload;
	private boolean notwice = false;
	
	private AsyncFileDownloader down = null; // Placeholder for the downloader thread if it would be used.

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.musicdetails);
		buttondownload = (Button) findViewById(R.id.MusicDownloadAndPlay);
		
		buttondownload.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				downloadAndPlay();
			}});


	    webview = (WebView) findViewById(R.id.musictext);

		AjaxRequest req = getFetchParameters(pageID);
		pbh.showProgressDialog("Fetching desc...");
		req.execute(requesthandler);
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
	}
	
	
	
	@Override
	public void createExtraMenuOptions(Menu menu) {
		menu.add(0,AppConstants.MENU_PLAY   ,0,"Play").setIcon(android.R.drawable.ic_media_play);
		super.createExtraMenuOptions(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_PLAY:
			downloadAndPlay();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Extracts the file URL from the fetched data, and downloads the file
	 * @param obj
	 */
	public void downloadFile(String html) throws Exception {
		//String url = obj.getString("filename");
		notwice = false;

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
		
		//String tmp2 = html.substring(beg - 10, end + 10);

		beg = tmp.lastIndexOf("/");
		filename = tmp.substring(beg + 1);

		playPath = FileStorage.getExternalMediaRoot()+ "/" + FileStorage.MUSIC_PATH;
		FileStorage.ensureDirectory(playPath);
		playPath += getFname();
		
		File f = new File(playPath);
		if (f.exists()) {
			playmusic();
			return;
		}
		
		pbh.hideProgressDialog();
		pbh.showProgressDialog("Downloading song...");
		down = ContentDownloader.asyncDownload(tmp, FileStorage.MUSIC_PATH + getFname(), requesthandler);
		notwice = true;
	}
	
	public String getFname() {
		return "/m" + pageID + ".mp3";
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
		  pbh.hideProgressDialog();
		}
	}


	@Override
	public void save() {
		try {
			File f = new File(playPath);
			if (!f.exists()) return; // Until that file exists, there is nothing we can do really.
			
			String targetPath = FileStorage.getUserStoragePath("Music", filename);

			File tf = new File(targetPath);
			FileStorage.ensureDirectory(tf.getParent());
			FileStorage.copyFile(f, tf);
			
			Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			sonError(-1, e);
		}
	}
	
	

}
