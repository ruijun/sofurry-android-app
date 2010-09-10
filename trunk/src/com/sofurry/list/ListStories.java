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
import com.sofurry.ThumbnailDownloaderThread;
import com.sofurry.ViewStoryActivity;
import com.sofurry.model.Submission;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.Authentication;
import com.sofurry.util.ImageStorage;

public class ListStories extends AbstractContentList<Submission> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		AjaxRequest req = new AjaxRequest();

		req.addParameter("f", "browse");
		req.addParameter("viewSource", ""+source);
		req.addParameter("contentType", "0");
		req.addParameter("entriesPerPage", "30");
		req.addParameter("page", "" + page);
		return req;
	}
	
	

	@Override
	public void parseResponse(JSONObject obj) {
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


	@Override
	public void setSelectedIndex(int selectedIndex) {
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
	public void resetViewSource(int newViewSource) {
		Log.i("SF", "ResetViewSource: "+newViewSource);
		viewSource = newViewSource;
		currentPage = 0;
		lastScrollY = 0;
		resultList = new ArrayList<Submission>();
		pageIDs = new ArrayList<String>();
		loadPage(currentPage, viewSource, true);
	}

}