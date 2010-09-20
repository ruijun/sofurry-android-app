package com.sofurry.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sofurry.requests.AsyncFileDownloader;
import com.sofurry.requests.IRequestHandler;

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
		Log.d("SF ContentDownloader", "Fetching image...");
		
		URL myImageURL = new URL(HttpRequest.encodeURL(url));
		HttpURLConnection connection = null;
		InputStream is = null;
		Bitmap bitmap = null;
		ByteArrayOutputStream bos = null;
		try {
			connection = (HttpURLConnection) myImageURL.openConnection();
			connection.setDoInput(true);
			connection.connect();
			if (connection.getContentType().toLowerCase().startsWith("text")) throw new Exception("Unable to download image. (text answer where binary expected)");
			is = connection.getInputStream();
			Log.d("SF ContentDownloader", "Downloading...");
			bos = new ByteArrayOutputStream();
		    int readBytes;
		    byte[] buffer = new byte[1024];
		    while ((readBytes = is.read(buffer)) > 0) {
		        bos.write(buffer, 0, readBytes);
		    }
		    bos.flush();
		    bos.close();
			//Log.d("SF ContentDownloader", is.available() + " bytes available to be read from server");
			Log.d("SF ContentDownloader", "creating drawable... ("+bos.size()+" bytes)");
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
	public static AsyncFileDownloader asyncDownload(String url, String filename, IRequestHandler req) {
		return AsyncFileDownloader.doRequest(req, url, filename, -1);
	}
	
	/**
	 * Downloads a file, and signals the complete download
	 * @param url
	 * @param filename
	 * @param feedback
	 * @throws Exception
	 */
	public static void downloadFile(String url, String filename) throws Exception {
		Log.d("SF ContentDownloader", "Fetching file...");
		URL myImageURL = new URL(HttpRequest.encodeURL(url));
		HttpURLConnection connection = (HttpURLConnection) myImageURL.openConnection();
		connection.setDoInput(true);
		connection.connect();
		if (connection.getContentType().toLowerCase().startsWith("text")) throw new Exception("Unable to download file. (text answer where binary expected)");
		//Log.d("contDown", "contentType" + connection.getContentType() + " / " + connection.getContentLength());
		InputStream is = connection.getInputStream();
		FileOutputStream os = FileStorage.getFileOutputStream(filename);
		try {
			byte[] buf = new byte[1024];
			int l;
	        while ((l = is.read(buf)) != -1) {
		      os.write(buf, 0, l);
		    }
		} catch (Exception e) {
			throw e;
		} finally {
			if (is != null)
	          is.close();
			if (os != null)
	          os.close();
		}
	}

	
}
