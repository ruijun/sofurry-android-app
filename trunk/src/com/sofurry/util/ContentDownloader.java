package com.sofurry.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ContentDownloader {

	public static Bitmap downloadBitmap(String url) {
		try {
			Log.d("SF ContentDownloader", "Fetching image...");
			URL myImageURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) myImageURL.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream is = connection.getInputStream();
			Log.d("SF ContentDownloader", is.available() + " bytes available to be read from server");
			Log.d("SF ContentDownloader", "creating drawable...");
			Bitmap bitmap = BitmapFactory.decodeStream(is);

			return bitmap;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	
}
