package com.sofurry;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;

import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.HttpRequest;

/**
 * A thread that fetches data from the SoFurry API, sending the result, or error to the MessageHandler
 * provided by the ContentController.
 *
 * @param <T>
 * The Type of result data expected
 * 
 * @deprecated
 */
public class ContentRequestThread<T> extends Thread {
	
	//private Map<String, String> requestParameters;
	//private Map<String, String> originalRequestParameters;
	private AjaxRequest request = null;
	private ContentController<T> controller;
	private Handler handler;
	protected ArrayList<T> resultList;
	//private String requestUrl;
	
//	public ContentRequestThread(ContentController<T> controller, Handler handler, String requestUrl, Map<String, String> requestParams) {
//		this.controller = controller;
//		this.handler = handler;
//		this.requestParameters = requestParams;
//		this.requestUrl = requestUrl;
//	}

	/**
	 * Creates a Request thread, that will attemt to fetch the data, specified in the request object
	 * @param controller
	 * The controller to answer to
	 * @param request
	 * The Request to perform
	 */
	public ContentRequestThread(ContentController controller, AjaxRequest request) {
		this.controller = controller;
		//this.handler = controller.getHandler();
		this.request = request;
	}

	// Asynchronous http request and result parsing
	public void run() {
		try {
			// add authentication parameters to the request
			request.authenticate();
			HttpResponse response = HttpRequest.doPost(request.getUrl(), request.getParameters());
			String httpResult = EntityUtils.toString(response.getEntity());
			resultList = new ArrayList<T>();
			//try {
				// Retry request with new otp sequence if it failed for the first time
				request.authenticate();
				//requestParameters = Authentication.addAuthParametersToQuery(requestParameters);
				response = HttpRequest.doPost(request.getUrl(), request.getParameters());
				httpResult = EntityUtils.toString(response.getEntity());

//				String errorMessage = controller.parseErrorMessage(httpResult);
//				if (errorMessage == null) {
//					controller.parseResponse(httpResult, resultList);
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}

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
