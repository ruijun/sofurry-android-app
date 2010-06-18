package com.sofurry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListAdapter;

import com.sofurry.list.SubmissionAdapter;
import com.sofurry.model.Submission;

public class ListStories extends AbstractContentList<Submission> {

	private ArrayList<String> pageIDs;
	
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
	protected int parseResponse(java.lang.String httpResult, ArrayList<Submission> list) throws JSONException {
		int numResults;
		Log.i("Stories.parseResponse", "response: " + httpResult);
		pageIDs = new ArrayList<String>();
		
		JSONObject jsonParser = new JSONObject(httpResult);
		JSONArray pagecontents = new JSONArray(jsonParser.getString("pagecontents"));
		JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
		numResults = items.length();
		for (int i = 0; i < numResults; i++) {
			Submission s = new Submission();
			s.setName(items.getJSONObject(i).getString("name"));
			s.setId(Integer.parseInt(items.getJSONObject(i).getString("pid")));
			s.setTags(items.getJSONObject(i).getString("keywords"));
			list.add(s);
			pageIDs.add(items.getJSONObject(i).getString("pid"));
		}
		return numResults;
	}

	@Override
	protected void setSelectedIndex(int selectedIndex) {
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("ListStories", "Viewing story ID: "+pageID);
		Intent i = new Intent( this, ViewStoryActivity.class ) ;
		i.putExtra("pageID", pageID) ;
		i.putExtra("useAuthentication", useAuthentication()) ;
		startActivity(i) ;
	}
	
	@Override
	protected boolean useAuthentication() {
		return false;
	}
	
	@Override 
	protected ListAdapter getAdapter(Context context) {
		return new SubmissionAdapter(context, R.layout.listitemtwolineicon, resultList);
	}

}