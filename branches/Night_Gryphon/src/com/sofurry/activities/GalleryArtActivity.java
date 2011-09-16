package com.sofurry.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;

import com.sofurry.AppConstants;
import com.sofurry.adapters.SubmissionGalleryAdapter;
import com.sofurry.base.classes.AbstractContentGallery;
import com.sofurry.base.classes.ActivityManager;
import com.sofurry.model.Submission;
import com.sofurry.model.Submission.SUBMISSION_TYPE;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.storage.ImageStorage;
import com.sofurry.util.ErrorHandler;

public class GalleryArtActivity extends AbstractContentGallery<Submission> {

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
		if (source == AppConstants.VIEWSOURCE_USER)
		  req.addParameter("authorid", viewSearch);
		req.addParameter("contentType", "" + contentType);
		req.addParameter("entriesPerPage", "" + entries);
		req.addParameter("page", "" + page);
		return req;
	}

	/**
	 * Converts Json-submission objects into a list of Submission objects
	 * @param obj
	 * The base JSON object as returned by the fetcher thread
	 * @throws JSONException
	 */
	public static void jsonToResultlist(JSONObject obj, ActivityManager<Submission> man, SUBMISSION_TYPE typ) throws JSONException {
		JSONArray pagecontents = new JSONArray(obj.getString("pagecontents"));
		JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
		for (int i = 0; i < items.length(); i++) {
			Submission s = new Submission();
			s.setType(typ);
			s.populate(items.getJSONObject(i));

			man.getResultList().add(s);
			//man.getPageIDs().add("" + s.getId());
		}
	}

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		return createBrowse(page, source, man.getViewSearch(), AppConstants.CONTENTTYPE_ART, AppConstants.ENTRIESPERPAGE_GALLERY);
	}
	
	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentGallery#parseResponse(org.json.JSONObject)
	 */
	public void parseResponse(JSONObject obj) {
		try {
			jsonToResultlist(obj, man, SUBMISSION_TYPE.ARTWORK);
		} catch (Exception e) {
			man.onError(-1,e);
		}
		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		Submission s = getDataItem(selectedIndex);
//		int pageID = Integer.parseInt(man.getPageIDs().get(selectedIndex));
		Log.i(AppConstants.TAG_STRING, "GalleryArt: Viewing art ID: " + s.getId());
		Intent i = new Intent(this, ViewArtActivity.class);
		s.feedIntent(i);
//		i.putExtra("pageID", s.getId());
//		i.putExtra("name", s.getName());
		man.getResultList().get(selectedIndex).feedIntent(i);
		startActivity(i);
	}


	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionGalleryAdapter(context, man.getResultList());
	}

	public void resetViewSourceExtra(int newViewSource) {
	}

	@Override
	public void finish() {
		// Cleans up the image storage, so we will not clutter the device with unwanted images
		try {
			ImageStorage.cleanupImages();
		} catch (Exception e) {
			// If this fails, its no biggie, but something might be interesting
			ErrorHandler.justLogError(e);
		}
		super.finish();
	}
	
	

}
