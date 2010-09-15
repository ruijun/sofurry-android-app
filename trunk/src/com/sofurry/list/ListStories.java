package com.sofurry.list;

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

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		return GalleryArt.createBrowse(page,source,man.getViewSearch(),AppConstants.CONTENTTYPE_STORIES,10);
	}
	

	@Override
	public void parseResponse(JSONObject obj) {
		try {
			GalleryArt.jsonToResultlist(obj, man, SUBMISSION_TYPE.STORY);
		} catch (Exception e) {
			man.ronError(e);
		}
		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}



	@Override
	public void setSelectedIndex(int selectedIndex) {
		Submission s = getDataItem(selectedIndex);
		//int pageID = Integer.parseInt(man.getPageIDs().get(selectedIndex));
		Log.i("ListStories", "Viewing story ID: " + s.getId());
		Intent i = new Intent(this, ViewStoryActivity.class);
		i.putExtra("pageID", s.getId());
		//i.putExtra("useAuthentication", useAuthentication());
		startActivity(i);
	}

	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
	}

	public void resetViewSourceExtra(int newViewSource) {
	}

	

}