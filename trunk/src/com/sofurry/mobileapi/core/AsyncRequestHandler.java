package com.sofurry.mobileapi.core;

import org.json.JSONObject;

import android.util.Log;

/**
 * @author Rangarig
 * 
 * Handler for Async Requests, that will create a thread, execute the request and call the callback.
 *
 */
public class AsyncRequestHandler extends Thread {
	private Request toHandle = null;  // The request to handle
	private CallBack callback = null; // The callback objecct to return the calls to
	
	/**
	 * @param toHandle sets the request to be handled
	 */
	public void setToHandle(Request toHandle) {
		this.toHandle = toHandle;
	}

	/**
	 * @param callback the callback object to use for callbacks
	 */
	public void setCallback(CallBack callback) {
		this.callback = callback;
	}

	/**
	 * Creates a Thread that will execute the request. Once the execution is complete
	 * the passed parameters will be called depending on the result of the thread.
	 * @param req
	 * The request to be executed
	 * @param cb
	 * The callback object, containing the methods to be called on return
	 */
	public static void go(Request req,CallBack cb) throws Exception {
		AsyncRequestHandler arh = new AsyncRequestHandler();
		arh.setCallback(cb);
		arh.setToHandle(req);
		arh.start();
	}

	@Override
	public void run() {
		try {
			// Execute the request
			JSONObject tmp = toHandle.execute();
			// Return the result
			callback.success(tmp);
		} catch (Exception e) {
			// In case anything goes wrong
			try {
				callback.fail(new RequestException(e, toHandle));
			} catch (Exception e2) {
				Log.d("[SOFURRY]", "ASyncRequest:Error while handling request Exception." + e2.getMessage());
			}
		}
	}
	
	
	
}
