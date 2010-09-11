package com.sofurry.gallery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;

import com.sofurry.AbstractContentGallery;
import com.sofurry.AppConstants;
import com.sofurry.model.Submission;
import com.sofurry.model.Submission.SUBMISSION_TYPE;
import com.sofurry.requests.AjaxRequest;

public class GalleryArt extends AbstractContentGallery<Submission> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		AjaxRequest req = new AjaxRequest();
		req.addParameter("f", "browse");
		req.addParameter("viewSource", ""+source);
		if (source == AppConstants.VIEWSOURCE_SEARCH)
		  req.addParameter("search", viewSearch);
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
			for (int i = 0; i < items.length(); i++) {
				
				Submission s = new Submission();
				s.setType(SUBMISSION_TYPE.ARTWORK);
				s.populate(items.getJSONObject(i));
				//s.loadSubmissionIcon();
				
				resultList.add(s);
				pageIDs.add("" + s.getId());
			}
		} catch (Exception e) {
			ronError(e);
		}
		// Start downloading the thumbnails
		startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		stopThumbDownloader();
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("GalleryArt", "Viewing art ID: " + pageID);
		Intent i = new Intent(this, PreviewArtActivity.class);
		i.putExtra("pageID", pageID);
		resultList.get(selectedIndex).feedIntent(i);
		startActivity(i);
	}


	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionGalleryAdapter(context, resultList);
	}

	@Override
	public void resetViewSource(int newViewSource) {
		pageIDs = new ArrayList<String>();
		super.resetViewSource(newViewSource);
	}

}
