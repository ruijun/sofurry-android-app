package com.sofurry.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.BaseAdapter;

import com.sofurry.AppConstants;
import com.sofurry.adapters.SubmissionGalleryAdapter;
import com.sofurry.base.classes.AbstractContentGallery;
import com.sofurry.base.classes.ActivityManager;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.model.Submission;
import com.sofurry.model.Submission.SUBMISSION_TYPE;
import com.sofurry.storage.ImageStorage;
import com.sofurry.util.ErrorHandler;

public class GalleryArtActivity extends AbstractContentGallery<Submission> {

	/**
	 * Converts Json-submission objects into a list of Submission objects
	 * @param obj
	 * The base JSON object as returned by the fetcher thread
	 * @throws JSONException
	 */
	public static void jsonToResultlist(JSONObject obj, ActivityManager<Submission> man, SUBMISSION_TYPE typ) throws JSONException {
		JSONArray pagecontents = new JSONArray(obj.getString("pagecontents"));
		man.totalPages = Integer.parseInt(obj.getString("totalpages"));
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
	public Request getFetchParameters(int page, ViewSource source) throws Exception {
		Request req = ApiFactory.createBrowse(source,null,ContentType.art,AppConstants.ENTRIESPERPAGE_GALLERY,page);
		return req;
	}
	
	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentGallery#parseResponse(org.json.JSONObject)
	 */
	public void parseResponse(JSONObject obj) {
		try {
			jsonToResultlist(obj, man, SUBMISSION_TYPE.ARTWORK);
		} catch (Exception e) {
			man.onError(e);
		}
		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		Submission s = getDataItem(selectedIndex);
		Log.i(AppConstants.TAG_STRING, "GalleryArt: Viewing art ID: " + s.getId());
		Intent i = new Intent(this, ViewArtActivity.class);
		s.feedIntent(i);
		// allow viewer to know submissions list
		i.putExtra("list", man.getResultList()); 
		i.putExtra("listId", selectedIndex); 
		startActivity(i);
	}


	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionGalleryAdapter(context, man.getResultList());
	}

	public void resetViewSourceExtra(ViewSource newViewSource) {
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(this);
		galleryView.setColumnWidth(prefs.getInt(AppConstants.PREFERENCE_THUMB_SIZE, 130));
	}
	

	

}
