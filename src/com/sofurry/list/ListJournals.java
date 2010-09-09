package com.sofurry.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListAdapter;

import com.sofurry.AbstractContentList;
import com.sofurry.R;
import com.sofurry.ThumbnailDownloaderThread;
import com.sofurry.ViewStoryActivity;
import com.sofurry.model.Submission;
import com.sofurry.util.Authentication;

public class ListJournals extends AbstractContentList<Submission> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	protected Map<String, String> getFetchParameters(int page, int source) {
		Map<String, String> kvPairs = new HashMap<String, String>();

		kvPairs.put("f", "browse");
		kvPairs.put("viewSource", ""+source);
		kvPairs.put("contentType", "3");
		kvPairs.put("entriesPerPage", "30");
		kvPairs.put("page", "" + page);
		return kvPairs;
	}
	
	

	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentList#parseResponse(org.json.JSONObject)
	 * Parses the response data from the JSONObject 
	 */
	@Override
	protected void parseResponse(JSONObject obj) {
		try {
			JSONArray pagecontents = new JSONArray(obj.getString("pagecontents"));
			JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
			numResults = items.length();
			for (int i = 0; i < numResults; i++) {
				Submission s = new Submission();
				s.populate(items.getJSONObject(i));
				s.loadUserIcon();

				resultList.add(s);
				pageIDs.add("" + s.getId());
			}
		} catch (Exception e) {
			ronError(e);
		}

		// Start downloading the thumbnails
		thumbnailDownloaderThread = new ThumbnailDownloaderThread(true, requesthandler, resultList);
		thumbnailDownloaderThread.start();
	}

//	public int parseResponse(java.lang.String httpResult, ArrayList<Submission> list) throws JSONException {
//		int numResults;
//		Log.i("Journals.parseResponse", "response: " + httpResult);
//
//		if (resultList != null)
//			list.addAll(resultList);
//
//		JSONObject jsonParser = new JSONObject(httpResult);
//		JSONArray pagecontents = new JSONArray(jsonParser.getString("pagecontents"));
//		JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
//		numResults = items.length();
//		for (int i = 0; i < numResults; i++) {
//			Submission s = new Submission();
//			s.setName(items.getJSONObject(i).getString("name"));
//			s.setId(Integer.parseInt(items.getJSONObject(i).getString("pid")));
//			s.setDate(items.getJSONObject(i).getString("date"));
//			s.setAuthorName(items.getJSONObject(i).getString("authorName"));
//			s.setAuthorID(items.getJSONObject(i).getString("authorId"));
//			s.setContentLevel(items.getJSONObject(i).getString("contentLevel"));
//			s.setTags(items.getJSONObject(i).getString("keywords"));
//			s.setThumbnailUrl(items.getJSONObject(i).getString("thumb"));
//			Bitmap thumb = IconStorage.loadUserIcon(Integer.parseInt(s.getAuthorID()));
//			if (thumb != null)
//				s.setThumbnail(thumb);
//
//			list.add(s);
//			pageIDs.add(items.getJSONObject(i).getString("pid"));
//		}
//
//		// Start downloading the thumbnails
//		thumbnailDownloaderThread = new ThumbnailDownloaderThread(true, handler, list);
//		thumbnailDownloaderThread.start();
//		return numResults;
//	}

	@Override
	protected void setSelectedIndex(int selectedIndex) {
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("ListJournals", "Viewing journal ID: " + pageID);
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
