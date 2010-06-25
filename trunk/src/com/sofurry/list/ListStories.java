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
import com.sofurry.ContentController;
import com.sofurry.R;
import com.sofurry.ThumbnailDownloaderThread;
import com.sofurry.ViewStoryActivity;
import com.sofurry.model.Submission;
import com.sofurry.util.Authentication;
import com.sofurry.util.IconStorage;

public class ListStories extends AbstractContentList<Submission> implements ContentController<Submission> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	protected Map<String, String> getFetchParameters(int page, int source) {
		Map<String, String> kvPairs = new HashMap<String, String>();

		kvPairs.put("f", "browse");
		kvPairs.put("viewSource", ""+source);
		kvPairs.put("contentType", "0");
		kvPairs.put("entriesPerPage", "30");
		kvPairs.put("page", "" + page);
		return kvPairs;
	}

	public int parseResponse(String httpResult, ArrayList<Submission> list) throws JSONException {
		int numResults;
		Log.i("Stories.parseResponse", "response: " + httpResult);

		if (resultList != null)
			list.addAll(resultList);

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
			pageIDs.add("" + s.getId());
		}

		// Start downloading the thumbnails
		thumbnailDownloaderThread = new ThumbnailDownloaderThread(true, handler, list);
		thumbnailDownloaderThread.start();
		return numResults;
	}

	@Override
	protected void setSelectedIndex(int selectedIndex) {
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("ListStories", "Viewing story ID: " + pageID);
		Intent i = new Intent(this, ViewStoryActivity.class);
		i.putExtra("pageID", pageID);
		i.putExtra("useAuthentication", useAuthentication());
		startActivity(i);
	}

	public boolean useAuthentication() {
		return (Authentication.getUsername() != null && Authentication.getUsername().trim().length() > 0);
	}

	@Override
	protected ListAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, resultList);
	}

	@Override
	protected void resetViewSource(int newViewSource) {
		Log.i("SF", "ResetViewSource: "+newViewSource);
		viewSource = newViewSource;
		currentPage = 0;
		lastScrollY = 0;
		resultList = new ArrayList<Submission>();
		pageIDs = new ArrayList<String>();
		loadPage(currentPage, viewSource, true);
	}

}