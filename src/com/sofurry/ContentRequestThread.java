package com.sofurry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import android.os.Handler;
import android.os.Message;

import com.sofurry.util.Authentication;
import com.sofurry.util.HttpRequest;

public class ContentRequestThread<T> extends Thread {
	
	private Map<String, String> requestParameters;
	private Map<String, String> originalRequestParameters;
	private ContentController<T> controller;
	private Handler handler;
	protected ArrayList<T> resultList;
	private String requestUrl;
	
	public ContentRequestThread(ContentController<T> controller, Handler handler, String requestUrl, Map<String, String> requestParams) {
		this.controller = controller;
		this.handler = handler;
		this.originalRequestParameters = requestParams;
		this.requestUrl = requestUrl;
	}
	
	// Asynchronous http request and result parsing
	public void run() {
		try {
			requestParameters = originalRequestParameters;
			if (controller.useAuthentication()) {
				// add authentication parameters to the request
				requestParameters = Authentication.addAuthParametersToQuery(originalRequestParameters);
			}
			HttpResponse response = HttpRequest.doPost(requestUrl, requestParameters);
			String httpResult = EntityUtils.toString(response.getEntity());
			resultList = new ArrayList<T>();
			try {
				if (controller.useAuthentication() && Authentication.parseResponse(httpResult) == false) {
					// Retry request with new otp sequence if it failed for the first time
					requestParameters = Authentication.addAuthParametersToQuery(originalRequestParameters);
					response = HttpRequest.doPost(requestUrl, requestParameters);
					httpResult = EntityUtils.toString(response.getEntity());
				}
				String errorMessage = controller.parseErrorMessage(httpResult);
				if (errorMessage == null) {
					controller.parseResponse(httpResult, resultList);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        Message msg = handler.obtainMessage();
        msg.obj = resultList;
        handler.sendMessage(msg);
	}


}
