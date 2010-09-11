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

	void parseResponse(JSONObject obj);
	
	/**
	 * Terminates the thumbnail downloading thread
	 */
	void stopThumbDownloader();
	
	/**
	 * Initializes the thumbnail downloading thread
	 * @param mode
	 */
	void startThumbnailDownloader();


}
