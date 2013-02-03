package com.sofurry.mobileapi.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sofurry.mobileapi.SFConstants;

import android.util.Log;

/**
 * @author Rangarig
 *
 * The request objects contains the properties and methods to execute a request against the SoFurry api.
 * 
 */
public class Request {

	/**
	 * The Viewsource Parameter presets
	 */
	public enum HttpMode {post,get};

	private static Object requestSync = new Object();
	
	protected Map<String, String> parameters = new HashMap<String, String>(); // The request parameters to be used
	protected String url = null; // The requests's URL to use
	protected int id = -1;	  // The specific request ID
	protected HttpMode mode = HttpMode.post; // The mode of this request

    /**
     * Adds or updates authentification information to this request
     */
    private void authenticate() {
        AuthenticationHandler.addAuthParametersToQuery(parameters);
    }
	
	/**
	 * The requests URL to be used for all Requests
	 * @param url
	 */
	public void setURL(String url) {
		this.url = url;
	}
	
	/**
	 * Changes the mode of the Request. 
	 * @param mode
	 * The mode of the request. Get or Post
	 */
	public void setMode(HttpMode mode) {
		this.mode = mode;
	}
	
	/**
	 * Sets a certain parameter for the request
	 * @param name
	 * The name of the parameter
	 * @param value
	 * The value of the parameter
	 * If the passed value is NULL, the parameter pair will be ignored.
	 */
	public void setParameter(String name, String value) {
		if (null == value) return;
		parameters.put(name, value);
	}

	
	/**
	 * Does the actual HTTP request and returns the answer as a raw sting
	 * @return
	 * The answer by the HTTP interface
	 * @throws Exception
	 */
	private String doRequest() throws Exception {
		authenticate(); // Authenticates the request (adding oth parameters to the parameters)

		String localurl = HttpRequestHandler.encodeURL(this.url);
        HttpResponse response = null;
        switch (this.mode) {
		case get:
	          response = HttpRequestHandler.doGet(localurl);
			break;

		default:
	          response = HttpRequestHandler.doPost(localurl, parameters);
			break;
		}
		
		return EntityUtils.toString(response.getEntity());
	}
	
	/**
	 * Executes the command and returns the result as JSON object
	 * @return
	 */
	public JSONObject execute() throws Exception {

		String tmp = null;
		
		synchronized (mode) { // Synchronize requests, so that the authorization will not fail because of sequence errors.
			
			tmp = doRequest();
			
			if (!AuthenticationHandler.parseResponse(tmp)) { // Try authentification again, in case the first request fails
				// Retry request with new otp sequence if it failed for the first time
				tmp = doRequest();
				if (!AuthenticationHandler.parseResponse(tmp)) {
				  throw new Exception("Authentification Failed (2nd attempt)."); // Check the sequence reply
				}
			}
		
		}
		tmp = postProcessString(tmp); // Allows customized post-processing of the result string
		
		JSONObject pagecontents = null;
		// Parse the result
		try {
			try {
				pagecontents = new JSONObject(tmp);
			} catch (JSONException jse) { // in case the returned value is a JSON Array, not an object
				JSONArray arr = new JSONArray(tmp); 
				pagecontents = new JSONObject();
				pagecontents.accumulate("array", arr);
			}
		} catch (Exception e) {
			Log.d(SFConstants.TAG_STRING, "Error parsing result data");
			Log.d(SFConstants.TAG_STRING, e.getMessage());
			Log.d(SFConstants.TAG_STRING, url);
			throw new RequestParsingException("Error Parsing result data", e);
		}
		
		checkError(pagecontents); // User specified error checking

		JSONObject post = postProcess(pagecontents); // User specified post processing on the result 
		
		post.accumulate("request_id", "" + id);
		return post;
	}
	
	/**
	 * Allows to transform the string before it is fed into the JSONParser... just
	 * in case the returned string is not JSONCompliant. Can be overriden by the 
	 * implementation of an api call
	 * @param toprocess
	 * The passed string, to be transformed
	 * @return
	 * Returns the string so it can be parsed by the JSON Parser
	 * @throws Exception
	 */
	protected String postProcessString(String toprocess) throws Exception { 
		return toprocess;
	}
	
	/**
	 * Checks the returned json object for error messages. Is overridable, to perform special error checking
	 * @param object
	 */
	protected void checkError(JSONObject object) throws Exception {
		// Intentionally left blank. This method is to be overriden bei the implementation
	}
	
	/**
	 * Can be overriden to have the request perform post processing operations on the result from the api call.
	 * @param object
	 * The post processed object
	 * @return
	 */
	protected JSONObject postProcess(JSONObject object) throws Exception {
		return object; // NOP
	}
	
	/**
	 * Makes an asyncronous call to the api. The actual request will be handled
	 * in a background thread, and once the return data is complete, the specified 
	 * method is called.
	 * @param callback
	 * Defines the method to be called on request completion. Example implementation:
	 * 
	 * request.executeAsync(
	 *   new CallBack(){ 
	 *     public void success(JSONObject result){ mymethod(result); };
	 *     public void fail(Exception e){ errormethod(e); };
	 *   };
	 * );
	 * 
	 * @return
	 * Does not return anything. The requests return value will be passed to the specified callback method.
	 */
	public void executeAsync(CallBack callback) throws Exception {
		AsyncRequestHandler.go(this, callback);
	}
	
}