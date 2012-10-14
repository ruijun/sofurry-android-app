package com.sofurry.mobileapi.downloaders;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sofurry.AppConstants;
import com.sofurry.mobileapi.SFConstants;
import com.sofurry.mobileapi.core.CallBack;
import com.sofurry.mobileapi.core.HttpRequestHandler;
import com.sofurry.storage.FileStorage;

public class ContentDownloader {

	/**
	 * Downloads a text string from the server
	 * @param url
	 * The url to the bitmap
	 * @return
	 * Returns downloaded text in String object
	 * @throws Exception
	 */
	public static String downloadText(String url) throws Exception {
		Log.d(SFConstants.TAG_STRING, "ContentDownloader: Fetching text...");
		
		URL myImageURL = new URL(HttpRequestHandler.encodeURL(url));
		HttpURLConnection connection = null;
		InputStream is = null;
		Bitmap bitmap = null;
		ByteArrayOutputStream bos = null;
		int t = 0; // Total bytes transfered
		int len = -1;
		
		try {
			connection = (HttpURLConnection) myImageURL.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.connect();
			connection.setReadTimeout(10);
			
			//connection.setReadTimeout(1000*10); // Set timeout for 10 seconds
			if (connection.getResponseCode() / 100 != 2)
				throw new Exception("HTTP error "+connection.getResponseCode()+" : "+connection.getResponseMessage());

			if (! connection.getContentType().toLowerCase().startsWith("text")) 
				throw new Exception("Content type should be text/* but get "+connection.getContentType().toLowerCase());

			len = connection.getContentLength(); // Maybe one needs to do this nowerdays. How am I supposed to know?
			is = connection.getInputStream();
			Log.d(AppConstants.TAG_STRING, "ContentDownloader: Downloading text...");
			bos = new ByteArrayOutputStream();
		    int readBytes;
		    byte[] buffer = new byte[1024];
		    while ((readBytes = is.read(buffer)) > 0) {
		        bos.write(buffer, 0, readBytes);
		        t += readBytes;

//		        if (canceler.isCanceled()) break;
		    }
		    bos.flush();
		    bos.close();
		} catch (SocketException se) {
			if ((t < 1024) || (t < len) || (!se.getMessage().toLowerCase().contains("reset")))
			throw se;
		} catch (Exception e) {
			throw e;
		} finally {
		  if (is != null)
		    is.close();
		  if (bos != null)
			bos.close();
		  connection.disconnect();
		}
		return bos.toString();
	}
	
	/**
	 * Downloads a bitmap from the server
	 * @param url
	 * The url to the bitmap
	 * @return
	 * Returns the bitmap as ready bitmap object
	 * @throws Exception
	 */
	public static Bitmap downloadBitmap(String url) throws Exception {
		Log.d(SFConstants.TAG_STRING, "ContentDownloader: Fetching image...");
		
		URL myImageURL = new URL(HttpRequestHandler.encodeURL(url));
		HttpURLConnection connection = null;
		InputStream is = null;
		Bitmap bitmap = null;
		ByteArrayOutputStream bos = null;
		int t = 0; // Total bytes transfered
		int len = -1;
		
		try {
			connection = (HttpURLConnection) myImageURL.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.connect();
			connection.setReadTimeout(10);
			
			//connection.setReadTimeout(1000*10); // Set timeout for 10 seconds
			if (connection.getContentType().toLowerCase().startsWith("text")) throw new Exception("Unable to download image. (text answer where binary expected)");
			len = connection.getContentLength(); // Maybe one needs to do this nowerdays. How am I supposed to know?
			is = connection.getInputStream();
			Log.d(AppConstants.TAG_STRING, "ContentDownloader: Downloading...");
			bos = new ByteArrayOutputStream();
		    int readBytes;
		    byte[] buffer = new byte[1024];
		    while ((readBytes = is.read(buffer)) > 0) {
		        bos.write(buffer, 0, readBytes);
		        t += readBytes;

//		        if (canceler.isCanceled()) break;
		    }
		    bos.flush();
		    bos.close();
			//Log.d("SF ContentDownloader", is.available() + " bytes available to be read from server");
			Log.d(AppConstants.TAG_STRING, "ContentDownloader: creating drawable... ("+bos.size()+" bytes)");
			bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(),0,bos.size());
		} catch (SocketException se) {
			if ((t < 1024) || (t < len) || (!se.getMessage().toLowerCase().contains("reset")))
			throw se;
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
	public static AsyncFileDownloader asyncDownload(String url, String absfilename, CallBack cb, PercentageFeedback feed, DownloadCancler downcancel, boolean deleteIncomplete) throws Exception {
		AsyncFileDownloader afd = new AsyncFileDownloader(url, absfilename, cb, feed, deleteIncomplete);
		afd.start();
		return afd;
	}

	/**
	 * Downloads a file, and signals the complete download
	 * @param url
	 * The URL to fetch the file from
	 * @param filename
	 * The filename to store the file to (absolute path required)
	 * @param feed
	 * An object implementing the .signalPercentage method, which will be called periodically to signal feedback
	 * @param canceler
	 * And object that is passed by the caller, allowing the caller to cancel the download
	 * @throws Exception
	 */
	public static void downloadFile(String url, String absfilename, PercentageFeedback feed, boolean deleteIncomplete) throws Exception {
		downloadFile(url,absfilename,feed,new DownloadCancler(), deleteIncomplete);
	}
	
	/**
	 * Downloads a file, and signals the complete download
	 * @param url
	 * The URL to fetch the file from
	 * @param filename
	 * The filename to store the file to (absolute path required)
	 * @param feed
	 * An object implementing the .signalPercentage method, which will be called periodically to signal feedback
	 * @param canceler
	 * And object that is passed by the caller, allowing the caller to cancel the download
	 * @throws Exception
	 */
	public static void downloadFile(String url, String absfilename, PercentageFeedback feed,DownloadCancler canceler, boolean deleteIncomplete) throws Exception {
		Log.d(SFConstants.TAG_STRING, "ContentDownloader: Fetching file...");
		Log.d(SFConstants.TAG_STRING, "ContentDownloader: " + url);
		URL myImageURL = new URL(HttpRequestHandler.encodeURL(url));
		HttpURLConnection connection = (HttpURLConnection) myImageURL.openConnection();
		connection.setDoInput(true);
		connection.connect();
		connection.setReadTimeout(10); // Set timeout for 10 seconds
		if (connection.getResponseCode() / 100 != 2)
			throw new Exception("HTTP error "+connection.getResponseCode()+" : "+connection.getResponseMessage());

		if (connection.getContentType().toLowerCase().startsWith("text")) 
			throw new Exception("Unable to download file. (text answer where binary expected)");

		int len = connection.getContentLength(); // Maybe one needs to do this nowerdays. How am I supposed to know?
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
		      if (feed != null) {

		    	  if (cnt++ > 50) {
		    		  feed.signalPercentage(t,0);
		    		  cnt = 0;
		    	  }
		      }
		      
		      if (canceler.isCanceled()) break;
		      
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
			if ((deleteIncomplete) && (t < len)) {
				File f = new File(absfilename);
				f.delete();
			}
		}
		// If the download was canceled, remove the file that we have written
		try {
			if (canceler.isCanceled()) {
				File fi = new File(absfilename);
			    fi.delete();
			}
		} catch (Exception e) {
		}
	}

	
}
