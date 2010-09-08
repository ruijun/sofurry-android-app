package com.sofurry.gallery;

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
import android.widget.BaseAdapter;

import com.sofurry.AbstractContentGallery;
import com.sofurry.ContentController;
import com.sofurry.PreviewArtActivity;
import com.sofurry.ThumbnailDownloaderThread;
import com.sofurry.model.Submission;
import com.sofurry.util.Authentication;
import com.sofurry.util.IconStorage;

public class GalleryArt extends AbstractContentGallery<Submission> implements ContentController<Submission> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	protected Map<String, String> getFetchParameters(int page, int source) {
		Map<String, String> kvPairs = new HashMap<String, String>();

		kvPairs.put("f", "browse");
		kvPairs.put("viewSource", ""+source);
		kvPairs.put("contentType", "1");
		kvPairs.put("entriesPerPage", "20");
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
			s.populate(items.getJSONObject(i));
			list.add(s);
			pageIDs.add("" + s.getId());
		}

		// Start downloading the thumbnails
		thumbnailDownloaderThread = new ThumbnailDownloaderThread(false, handler, list);
		thumbnailDownloaderThread.start();
		return numResults;
	}

	@Override
	protected void setSelectedIndex(int selectedIndex) {
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("GalleryArt", "Viewing art ID: " + pageID);
		Intent i = new Intent(this, PreviewArtActivity.class);
		i.putExtra("pageID", pageID);
		i.putExtra("name", resultList.get(selectedIndex).getName());
		i.putExtra("tags", resultList.get(selectedIndex).getTags());
		i.putExtra("authorName", resultList.get(selectedIndex).getAuthorName());
		i.putExtra("authorId", resultList.get(selectedIndex).getAuthorID());
		i.putExtra("thumbnail", resultList.get(selectedIndex).getThumbnailUrl());
		startActivity(i);
	}

	public boolean useAuthentication() {
		return (Authentication.getUsername() != null && Authentication.getUsername().trim().length() > 0);
	}

	@Override
	protected BaseAdapter getAdapter(Context context) {
		return new SubmissionGalleryAdapter(context, resultList);
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
