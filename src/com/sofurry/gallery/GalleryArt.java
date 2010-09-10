package com.sofurry.gallery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;

import com.sofurry.AbstractContentGallery;
import com.sofurry.ThumbnailDownloaderThread;
import com.sofurry.model.Submission;
import com.sofurry.requests.AjaxRequest;

public class GalleryArt extends AbstractContentGallery<Submission> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		AjaxRequest req = new AjaxRequest();
		req.addParameter("f", "browse");
		req.addParameter("viewSource", ""+source);
		req.addParameter("contentType", "1");
		req.addParameter("entriesPerPage", "20");
		req.addParameter("page", "" + page);
		return req;
	}
	
	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentGallery#parseResponse(org.json.JSONObject)
	 */
	public void parseResponse(JSONObject obj) {
		try {
			JSONArray pagecontents = new JSONArray(obj.getString("pagecontents"));
			JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
			numResults = items.length();
			for (int i = 0; i < numResults; i++) {
				
				Submission s = new Submission();
				s.populate(items.getJSONObject(i));
				s.loadSubmissionIcon();
				
				resultList.add(s);
				pageIDs.add("" + s.getId());
			}
		} catch (Exception e) {
			ronError(e);
		}
		// Start downloading the thumbnails
		thumbnailDownloaderThread = new ThumbnailDownloaderThread(false, requesthandler, resultList);
		thumbnailDownloaderThread.start();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("GalleryArt", "Viewing art ID: " + pageID);
		Intent i = new Intent(this, PreviewArtActivity.class);
		i.putExtra("pageID", pageID);
		resultList.get(selectedIndex).feedIntent(i);
		startActivity(i);
		if (thumbnailDownloaderThread != null) thumbnailDownloaderThread.stopThread();
	}


	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionGalleryAdapter(context, resultList);
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
