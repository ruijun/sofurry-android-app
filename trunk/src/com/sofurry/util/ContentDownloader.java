package com.sofurry.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.client.utils.URLEncodedUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sofurry.AppConstants;
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
		if (url.startsWith("images/")) url = AppConstants.SITE_URL + "/" + url;
		Log.d("SF ContentDownloader", "Fetching image...");
		
		URL myImageURL = new URL(HttpRequest.encodeURL(url));
		HttpURLConnection connection = null;
		InputStream is = null;
		Bitmap bitmap = null;
		try {
			connection = (HttpURLConnection) myImageURL.openConnection();
			connection.setDoInput(true);
			connection.connect();
			is = connection.getInputStream();
			//Log.d("SF ContentDownloader", is.available() + " bytes available to be read from server");
			Log.d("SF ContentDownloader", "creating drawable...");
			bitmap = BitmapFactory.decodeStream(is);
		} catch (Exception e) {
			throw e;
		} finally {
		  is.close();
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
