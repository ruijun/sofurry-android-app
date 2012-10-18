package com.sofurry.mobileapi.downloadmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

import android.util.Log;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.mobileapi.SFConstants;
import com.sofurry.mobileapi.core.HttpRequestHandler;
import com.sofurry.storage.FileStorage;

/**
 * Download request parameters holder
 * @author Night_Gryphon
 *
 */
public class HTTPFileDownloadTask extends abstractDownloadTask {

	private boolean fOverwrite = false;
	private boolean fDeleteIncomplete = true;
	private String fRequiredMimeType = null;
	private String fDeclinedMimeType = null;
	private String fURL = null;
	private String fFileName = null;
	private int fTimeout = 10;

	private HttpURLConnection connection = null; // need this to cancel

	

	public HTTPFileDownloadTask(	String aURL, String aFileName, 
							IJobStatusCallback aCallback, 
							int aTimeout,
							boolean aOverwrite,	boolean aDeleteIncomplete, 
							String aRequiredMimeType, String aDeclinedMimeType,
							int aRetryCount) {
		super(aCallback, aRetryCount);

		this.fOverwrite = aOverwrite;
		this.fDeleteIncomplete = aDeleteIncomplete;
		this.fRequiredMimeType = aRequiredMimeType;
		this.fURL = aURL;
		this.fFileName = aFileName;
		this.fDeclinedMimeType = aDeclinedMimeType;
		this.fTimeout = aTimeout;
	}
	
	@Override
	public void cancel() {
		if (connection != null)
			connection.disconnect();
		super.cancel();
	}

	@Override
	protected void doDownload() throws Exception {
		if  ((! fOverwrite) && ( (new File(fFileName)).exists()) )
			return;
		
		Log.d(SFConstants.TAG_STRING, "HTTPFileDownloader: Fetching file...");
		Log.d(SFConstants.TAG_STRING, "HTTPFileDownloader: " + fURL);
		URL myImageURL = new URL(HttpRequestHandler.encodeURL(fURL));
		HttpURLConnection connection = (HttpURLConnection) myImageURL.openConnection();
		connection.setDoInput(true);
		connection.connect();
		connection.setReadTimeout(fTimeout); // Set timeout for 10 seconds
		if (connection.getResponseCode() / 100 != 2)
			throw new Exception("HTTP error "+connection.getResponseCode()+" : "+connection.getResponseMessage());

		if (fRequiredMimeType != null)
		if (! connection.getContentType().toLowerCase().contains(fRequiredMimeType)) 
			throw new Exception("Wrong content type "+connection.getContentType());

		if (fDeclinedMimeType != null)
		if (connection.getContentType().toLowerCase().contains(fDeclinedMimeType)) 
			throw new Exception("Wrong content type "+connection.getContentType());

		int len = connection.getContentLength();
		InputStream is = connection.getInputStream();
		FileOutputStream os = FileStorage.getFileOutputStream(fFileName);
		int t = 0; // Total bytes transfered
		int l = 0;
		try {
			byte[] buf = new byte[1024];
			int cnt = 0;
	        while ((l = is.read(buf)) != -1) {
		      os.write(buf, 0, l);
		      t+=l;
		      
		      // If feedback is signalable, we will report back the download percentage
	    	  if (cnt++ > 50) {
	    		  onProgress(this, t, len, null);
	    		  cnt = 0;
		      }
		      
		      if (isCancelled) break;
		      
		    }
	        // I positively HATE this workaround. But as of now, after a day of fiddeling, I cannot do any better :(
		} catch (SocketException se) {
			if ((t < 1024) || (t < len) || (!se.getMessage().toLowerCase().contains("reset")))
			throw se;
		} catch (Exception e) {
		    Log.d(AppConstants.TAG_STRING, "Dwn: " + t + " " + l);
			throw e;
		} finally {
			if (is != null)
	          is.close();
			if (os != null) {
			  os.flush();
	          os.close();
			}
			if ((fDeleteIncomplete) && (t < len)) {
				File f = new File(fFileName);
				f.delete();
				throw new Exception("Incomplete file downloaded");
			}
		}
		// If the download was canceled, remove the file that we have written
		try {
			if (isCancelled) {
				File fi = new File(fFileName);
			    fi.delete();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public String getThreadName() {
		return fURL;
	}


}
