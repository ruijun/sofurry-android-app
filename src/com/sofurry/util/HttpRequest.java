package com.sofurry.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.sofurry.AppConstants;

import android.text.Html;
import android.util.Log;


public class HttpRequest {

	public static HttpResponse doPost(String url, Map<String, String> kvPairs)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setBooleanParameter("http.protocol.expect-continue", false); // Disable EXPECT because lighttpd doesn't like it

/*		httpclient.getCredentialsProvider().setCredentials(
                new AuthScope("dev.sofurry.com", 80), 
                new UsernamePasswordCredentials("sofurry", "l"));
*/
		HttpPost httppost = new HttpPost(url);
		if (kvPairs != null && kvPairs.isEmpty() == false) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
					kvPairs.size());
			String k, v;
			Iterator<String> itKeys = kvPairs.keySet().iterator();
			while (itKeys.hasNext()) {
				k = itKeys.next();
				if (k != null) {
					v = kvPairs.get(k);
					Log.d("HTTP", "k/v: "+k+" / "+v);
					nameValuePairs.add(new BasicNameValuePair(k, v));
				}
			}
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		}
		HttpResponse response;
		response = httpclient.execute(httppost);
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