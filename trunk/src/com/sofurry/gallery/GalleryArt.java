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

	/**
	 * Creates a browse command, to be used with the AJax interface
	 * @param page
	 * The page to get
	 * @param source
	 * the viewsource
	 * @param viewSearch
	 * the tags
	 * @param contentType
	 * the contenttype
	 * @param entries
	 * the number of entries to return
	 * @return
	 * Returns an ajax request with parameters
	 */
	public static AjaxRequest createBrowse(int page, int source, String viewSearch, int contentType, int entries) {
		AjaxRequest req = new AjaxRequest();
		req.addParameter("f", "browse");
		req.addParameter("viewSource", ""+source);
		if (source == AppConstants.VIEWSOURCE_SEARCH)
		  req.addParameter("search", viewSearch);
		req.addParameter("contentType", "" + contentType);
		req.addParameter("entriesPerPage", "" + entries);
		req.addParameter("page", "" + page);
		return req;
	}

	
	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		return createBrowse(page,source,man.getViewSearch(),AppConstants.CONTENTTYPE_ART,20);
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
				
				man.getResultList().add(s);
				pageIDs.add("" + s.getId());
			}
		} catch (Exception e) {
			man.ronError(e);
		}
		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("GalleryArt", "Viewing art ID: " + pageID);
		Intent i = new Intent(this, PreviewArtActivity.class);
		i.putExtra("pageID", pageID);
		man.getResultList().get(selectedIndex).feedIntent(i);
		startActivity(i);
	}


	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionGalleryAdapter(context, man.getResultList());
	}

	public void resetViewSourceExtra(int newViewSource) {
		pageIDs = new ArrayList<String>();
	}

}
