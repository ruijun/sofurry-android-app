package com.sofurry.mobileapi.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.sofurry.AppConstants;

import android.util.Log;


/**
 * @author Rangarig
 *
 * The HttpRequestHandler contains all the methods used to communicate via HTTP.
 */
public class HttpRequestHandler {

	/**
	 * Does a POST Request and returns the response 
	 * @param url
	 * The URL to do the POST request
	 * @param kvPairs
	 * The parameters for the POST request
	 * @return
	 * Returns an HTTPResponse object
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpResponse doPost(String url, Map<String, String> kvPairs) throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setBooleanParameter("http.protocol.expect-continue", false); // Disable EXPECT because lighttpd doesn't like it

		HttpPost httppost = new HttpPost(url);
		if (kvPairs != null && kvPairs.isEmpty() == false) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(kvPairs.size());
			String k, v;
			Iterator<String> itKeys = kvPairs.keySet().iterator();
			while (itKeys.hasNext()) {
				k = itKeys.next();
				if (k != null) {
					v = kvPairs.get(k);
					Log.d(AppConstants.TAG_STRING, "HTTP: k/v: "+k+" / "+v);
					nameValuePairs.add(new BasicNameValuePair(k, v));
				}
			}
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
		}
		HttpResponse response;
		response = httpclient.execute(httppost);
		return response;
	}
	
	/**
	 * Does a GET Request to a webserver and returns the response
	 * @param url
	 * The URL for the GET Request
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpResponse doGet(String url) throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setBooleanParameter("http.protocol.expect-continue", false); // Disable EXPECT because lighttpd doesn't like it

		HttpGet httpget = new HttpGet(url);
		HttpResponse response;
		response = httpclient.execute(httpget);
		return response;
	}
	
	/**
	 * Encodes an URL ready for use with the webserver
	 * @param url
	 * Die zu encodende URL
	 * @return
	 */
	public static String encodeURL(String url) {
		if (url.startsWith("content/")) url = AppConstants.SITE_URL + "/page/" + url;
		if (url.startsWith("images/")) url = AppConstants.SITE_URL + "/" + url;

		// since URLEncoder.encode(url, "UTF-8") is not the right way to do this, this will have to suffice:
		return url.replaceAll(" ", "%20");
	}
	
	/**
	 * Extracts the extension off an url
	 * @param url
	 * The URL to extract the extension from
	 * @return
	 * Returns the extension e.G. .jpg
	 */
	public static String extractExtension(String url) {
		int idx = url.lastIndexOf('.');
		if (idx == -1) return null;
		return url.substring(idx);
	}

}