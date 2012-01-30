package com.sofurry.mobileapi.core;

import org.json.JSONObject;


/**
 * @author Rangarig
 *
 * An interface that is used by the request object to define methods that will be called, once a request is complete
 * 
 */
public interface CallBack {
	
	/**
	 * Method that will be called once the request is complete
	 * @param obj
	 */
	void success(JSONObject obj);
	void fail(Exception e);
}
