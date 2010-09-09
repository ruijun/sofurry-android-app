package com.sofurry.requests;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sofurry.AppConstants;
import com.sofurry.util.Authentication;
import com.sofurry.util.HttpRequest;

/**
 * A thread that fetches data from the SoFurry API, sending the result, or error to the MessageHandler
 * provided.
 * 
 * The messagehandler can determine the outcome of the threaded request, by examinging the returned object type.
 * If the object is an Exception, an error has occured.
 * If the object is an Datatype, it should know what to do with it.
 *
 */
public class RequestThread extends Thread {
	
	//private Map<String, String> requestParameters;
	//private Map<String, String> originalRequestParameters;
	private AjaxRequest request = null;
	private Handler handler;

	/**
	 * Creates a Request thread, that will attemt to fetch the data, specified in the request object
	 * @param controller
	 * The controller to answer to
	 * @param request
	 * The Request to perform
	 */
	public RequestThread(IRequestHandler reqHandler, AjaxRequest request) {
		this.handler = reqHandler.getRequestHandler();
		this.request = request;
	}

	/**
	 * Parses returned HTML data, to determine if an Error Message was returned
	 * @param httpResult
	 * The HTML Data to examine
	 * @return
	 */
	public void parseErrorMessage(JSONObject parsed) throws Exception {
		try {
			// check for json error message and parse it
			int messageType = parsed.getInt("messageType");
			if (messageType == AppConstants.AJAXTYPE_APIERROR) {
				String error = parsed.getString("error");
				Log.d("List.parseErrorMessage", "Error: " + error);
				throw new Exception(error);
			}
		} catch (JSONException e) {
			Log.d("Auth.parseResponse", e.toString());
		}
	}

	
	// Asynchronous http request and result parsing
	public void run() {
		
		Object answer = null; // Will contain the answer that is returned to the client
		try {
			// add authentication parameters to the request
			request.authenticate();
			HttpResponse response = HttpRequest.doPost(request.getUrl(), request.getParameters());
			String httpResult = EntityUtils.toString(response.getEntity());
			
			if (!Authentication.parseResponse(httpResult)) { // Try authentification again, in case the first request fails
				// Retry request with new otp sequence if it failed for the first time
				request.authenticate();
				//requestParameters = Authentication.addAuthParametersToQuery(requestParameters);
				response = HttpRequest.doPost(request.getUrl(), request.getParameters());
				httpResult = EntityUtils.toString(response.getEntity());
				if (!Authentication.parseResponse(httpResult)) {
				  throw new Exception("Authentification Failed (2nd attempt)."); // Check the sequence reply
				}
			}
			
			if ("".equals(httpResult)) {
				answer = new JSONObject();
			} else {

				try {
					// Analyse results
					JSONObject jsonParser = new JSONObject(httpResult);
					// Check for error Message
					parseErrorMessage(jsonParser);
					answer = jsonParser; // Return results to caller
				} catch (JSONException je) { // In case that no JSON data is returned, but a whole lot of text, we will just return the text instead, for better analysis.
					answer = httpResult;
				}
			}
		} catch (ClientProtocolException e) {
			answer = e;
		} catch (IOException e) {
			answer = e;
		} catch (Exception e) {
			answer = e;
		}
		
		// Signal the result of the operation to our caller
        Message msg = handler.obtainMessage();
        msg.obj = answer;
        handler.sendMessage(msg);
	}


}
