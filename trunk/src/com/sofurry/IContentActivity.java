package com.sofurry;

import org.json.JSONObject;

import com.sofurry.requests.AjaxRequest;

/**
 * @author Rangarig
 * 
 * This interface is implemented by all ContentGalleries or ContentLists
 */
public interface IContentActivity {
	
	void setSelectedIndex(int selectedIndex);

	AjaxRequest getFetchParameters(int page, int source);

	void resetViewSource(int newViewSource);
	
	void parseResponse(JSONObject obj);

}
