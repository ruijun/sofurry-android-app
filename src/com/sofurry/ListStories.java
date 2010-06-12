package com.sofurry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

public class ListStories extends AbstractContentList<String> {

	@Override
	protected Map<String, String> getFetchParameters() {
		Map<String, String> kvPairs = new HashMap<String, String>();

		kvPairs.put("f", "browse");
		kvPairs.put("viewSource", "0");
		kvPairs.put("contentType", "0");
		kvPairs.put("entriesPerPage", "30");
		kvPairs.put("page", "10");
		return kvPairs;
	}

	@Override
	protected int parseResponse(java.lang.String httpResult, ArrayList<String> list) throws JSONException {
		int numResults;
		Log.i("Stories.parseResponse", "response: " + httpResult);

		JSONObject jsonParser = new JSONObject(httpResult);
		JSONArray pagecontents = new JSONArray(jsonParser.getString("pagecontents"));
		JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
		numResults = items.length();
		for (int i = 0; i < numResults; i++) {
			list.add(items.getJSONObject(i).getString("name"));
		}
		return numResults;
	}

	@Override
	protected void setSelectedIndex(int selectedIndex) {
		int pageID = 1;
		Intent i = new Intent( this, ViewStoryActivity.class ) ;
		i.putExtra("pageID", pageID) ;
		i.putExtra("useAuthentication", useAuthentication()) ;
		startActivity(i) ;
	}
	
	@Override
	protected boolean useAuthentication() {
		return false;
	}

}