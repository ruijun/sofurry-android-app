package com.sofurry.requests;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IRequestHandler;
import com.sofurry.storage.FileStorage;
import com.sofurry.util.ProgressSignal;

public class ContentDownloader {

	/**
	 * Downloads a bitmap from the server
	 * @param url
	 * The url to the bitmap
	 * @return
	 * Returns the bitmap as ready bitmap object
	 * @throws Exception
	 */
	public static Bitmap downloadBitmap(String url) throws Exception {
		Log.d(AppConstants.TAG_STRING, "ContentDownloader: Fetching image...");
		
		URL myImageURL = new URL(HttpRequest.encodeURL(url));
		HttpURLConnection connection = null;
		InputStream is = null;
		Bitmap bitmap = null;
		ByteArrayOutputStream bos = null;
		try {
			connection = (HttpURLConnection) myImageURL.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.connect();
			connection.setReadTimeout(10);
			
			//connection.setReadTimeout(1000*10); // Set timeout for 10 seconds
			if (connection.getContentType().toLowerCase().startsWith("text")) throw new Exception("Unable to download image. (text answer where binary expected)");
			is = connection.getInputStream();
			Log.d(AppConstants.TAG_STRING, "ContentDownloader: Downloading...");
			bos = new ByteArrayOutputStream();
		    int readBytes;
		    byte[] buffer = new byte[1024];
		    while ((readBytes = is.read(buffer)) > 0) {
		        bos.write(buffer, 0, readBytes);
		    }
		    bos.flush();
		    bos.close();
			//Log.d("SF ContentDownloader", is.available() + " bytes available to be read from server");
			Log.d(AppConstants.TAG_STRING, "ContentDownloader: creating drawable... ("+bos.size()+" bytes)");
			bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(),0,bos.size());
		} catch (Exception e) {
			throw e;
		} finally {
		  if (is != null)
		    is.close();
		  if (bos != null)
			bos.close();
		  connection.disconnect();
		}
		return bitmap;
	}
	
	/**
	 * Downloads a file and signals the completed download to the request handler
	 * @param url
	 * The url to fetch the file from
	 * @param filename
	 * The filename to store the file under
	 * @param req
	 * The request handler to signal arrival to
	 */
	public static AsyncFileDownloader asyncDownload2(String url, String absfilename, IRequestHandler req) {
		return AsyncFileDownloader.doRequest2(req, url, absfilename, AppConstants.REQUEST_ID_DOWNLOADFILE);
	}
	
	/**
	 * Downloads a file, and signals the complete download
	 * @param url
	 * The URL to fetch the file from
	 * @param filename
	 * The filename to store the file to (absolute path required)
	 * @param feedback
	 * A Request handler that will be bombarded with ProgressSignal objects, to signal the progress of things.
	 * @param parent
	 * The Thread this is called from. If it exists, it can be checked for cancelation requests
	 * @throws Exception
	 */
	public static void downloadFile(String url, String absfilename, IRequestHandler feedback) throws Exception {
		Log.d(AppConstants.TAG_STRING, "ContentDownloader: Fetching file...");
		URL myImageURL = new URL(HttpRequest.encodeURL(url));
		HttpURLConnection connection = (HttpURLConnection) myImageURL.openConnection();
		connection.setDoInput(true);
		connection.connect();
		connection.setReadTimeout(10); // Set timeout for 10 seconds
		if (connection.getContentType().toLowerCase().startsWith("text")) throw new Exception("Unable to download file. (text answer where binary expected)");
		//int len = connection.getContentLength(); // Maybe one needs to do this nowerdays. How am I supposed to know?
		//Log.d("contDown", "contentType" + connection.getContentType() + " / " + connection.getContentLength());
		InputStream is = connection.getInputStream();
		FileOutputStream os = FileStorage.getFileOutputStream(absfilename);
		int t = 0; // Total bytes transfered
		int l = 0;
		try {
			byte[] buf = new byte[1024];
			int cnt = 0;
	        while ((l = is.read(buf)) != -1) {
		      os.write(buf, 0, l);
		      t+=l;
		      
		      // If feedback is signalable, we will report back the download percentage
		      if (feedback != null) {

		    	  if (cnt++ > 50) {
		    		  feedback.postMessage(new ProgressSignal(t,0));
		    		  cnt = 0;
		    	  }
		    	  
		    	  if (feedback.isKilled()) throw new Exception("Transferthread was terminated by request handler.");
		      }
		      
		    }
	        // I positively HATE this workaround. But as of now, after a day of fiddeling, I cannot do any better :(
		} catch (SocketException se) {
			if ((t < 1024) || (!se.getMessage().toLowerCase().contains("reset")))
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
		}
	}

	
}
