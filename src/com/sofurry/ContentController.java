package com.sofurry;

import java.util.ArrayList;

import org.json.JSONException;


public interface ContentController<T> {

	public String parseErrorMessage(String httpResult);
	
	public int parseResponse(String httpResult, ArrayList<T> list) throws JSONException;

	public boolean useAuthentication();

	
}
