package com.sofurry.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ListAdapter;

import com.sofurry.AbstractContentList;
import com.sofurry.R;
import com.sofurry.ViewStoryActivity;
import com.sofurry.AbstractContentList.ThumbnailDownloadThread;
import com.sofurry.model.Submission;
import com.sofurry.util.IconStorage;

public class ListJournals extends AbstractContentList<Submission> {

	private ArrayList<String> pageIDs;
	
	@Override
	protected Map<String, String> getFetchParameters() {
		Map<String, String> kvPairs = new HashMap<String, String>();

		kvPairs.put("f", "browse");
		kvPairs.put("viewSource", "0");
		kvPairs.put("contentType", "3");
		kvPairs.put("entriesPerPage", "30");
		kvPairs.put("page", "0");
		return kvPairs;
	}

	@Override
	protected int parseResponse(java.lang.String httpResult, ArrayList<Submission> list) throws JSONException {
		int numResults;
		Log.i("Journals.parseResponse", "response: " + httpResult);
		pageIDs = new ArrayList<String>();
		
		JSONObject jsonParser = new JSONObject(httpResult);
		JSONArray pagecontents = new JSONArray(jsonParser.getString("pagecontents"));
		JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
		numResults = items.length();
		for (int i = 0; i < numResults; i++) {
			Submission s = new Submission();
			s.setName(items.getJSONObject(i).getString("name"));
			s.setId(Integer.parseInt(items.getJSONObject(i).getString("pid")));
			s.setDate(items.getJSONObject(i).getString("date"));
			s.setAuthorName(items.getJSONObject(i).getString("authorName"));
			s.setAuthorID(items.getJSONObject(i).getString("authorId"));
			s.setContentLevel(items.getJSONObject(i).getString("contentLevel"));
			s.setTags(items.getJSONObject(i).getString("keywords"));
			s.setThumbnailUrl(items.getJSONObject(i).getString("thumb"));
			Bitmap thumb = IconStorage.loadUserIcon(Integer.parseInt(s.getAuthorID()));
			if (thumb != null)
				s.setThumbnail(thumb);
			
			list.add(s);
			pageIDs.add(items.getJSONObject(i).getString("pid"));
		}
		//Start downloading the thumbnails
		thumbnailDownloadThread = new ThumbnailDownloadThread(true);
		thumbnailDownloadThread.start();
		return numResults;
	}

	@Override
	protected void setSelectedIndex(int selectedIndex) {
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("ListJournals", "Viewing journal ID: "+pageID);
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
