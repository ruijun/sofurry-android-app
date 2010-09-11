package com.sofurry.list;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;

import com.sofurry.AbstractContentList;
import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.ViewStoryActivity;
import com.sofurry.gallery.GalleryArt;
import com.sofurry.model.Submission;
import com.sofurry.model.Submission.SUBMISSION_TYPE;
import com.sofurry.requests.AjaxRequest;

public class ListStories extends AbstractContentList<Submission> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		return GalleryArt.createBrowse(page,source,man.getViewSearch(),AppConstants.CONTENTTYPE_STORIES,30);
	}
	

	@Override
	public void parseResponse(JSONObject obj) {
		try {
			JSONArray pagecontents = new JSONArray(obj.getString("pagecontents"));
			JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
			for (int i = 0; i < items.length(); i++) {
				Submission s = new Submission();
				s.populate(items.getJSONObject(i));
				s.setType(SUBMISSION_TYPE.STORY);

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
		Log.i("ListStories", "Viewing story ID: " + pageID);
		Intent i = new Intent(this, ViewStoryActivity.class);
		i.putExtra("pageID", pageID);
		//i.putExtra("useAuthentication", useAuthentication());
		startActivity(i);
	}

	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
	}

	public void resetViewSourceExtra(int newViewSource) {
		pageIDs = new ArrayList<String>();
	}

	

}