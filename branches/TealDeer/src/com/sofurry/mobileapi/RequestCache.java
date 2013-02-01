package com.sofurry.mobileapi;

import org.json.JSONObject;

import com.sofurry.mobileapi.core.Request;

/**
 * @author Rangarig
 * 
 * A Mechanism, that will cache the result of a request, and only re-request the data again if a certain threshold is passed.
 *
 */
public class RequestCache {
	
	private Request req = null;
	private long lastRequest = 0;
	private long threshold = 0;
	private JSONObject lastResult = null;
	
	/**
	 * @param req
	 * The request to handle in a cached matter
	 * @param threshold
	 * The number of miliseconds that have to have passed before the data is requested again.
	 */
	public RequestCache(Request req, long threshold) {
		this.req = req;
		this.threshold = threshold;
	}
	
	/**
	 * Executes the request, or just reports the cached reply
	 * @return
	 * Returns a JSON Object
	 * or... an exception, if something goes wrong
	 */
	public JSONObject execute() throws Exception {
		
		// Has it been too long since the last request? -> Purge the data
		if (System.currentTimeMillis() > lastRequest + threshold)
			lastResult = null;
		
		// Do we have cached data? Return it.
		if (lastResult != null) return lastResult;
		
		// Execute the request, and store data, then return it.
		lastResult = req.execute();
		lastRequest = System.currentTimeMillis();
		
		return lastResult;
	}

}
