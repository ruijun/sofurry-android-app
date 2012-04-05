package com.sofurry.activities;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.adapters.SubmissionListAdapter;
import com.sofurry.base.classes.AbstractContentList;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.model.Submission;

public class ListStoriesActivity extends AbstractContentList<Submission> {

//	public Request getFetchParameters(int page) throws Exception {
//		Request req = ApiFactory.createBrowse(man.getViewSource(),man.getViewSearch(),ContentType.stories,AppConstants.ENTRIESPERPAGE_GALLERY,page);
//		return req;
//		//return GalleryArtActivity.createBrowse(page,source,man.getViewSearch(),AppConstants.CONTENTTYPE_STORIES,AppConstants.ENTRIESPERPAGE_GALLERY);
//	}
	

	@Override
	public void parseResponse(JSONObject obj) {
		try {
			GalleryArtActivity.jsonToResultlist(obj, man);
		} catch (Exception e) {
			man.onError(e);
		}
		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}



	@Override
	public void setSelectedIndex(int selectedIndex) {
		Submission s = getDataItem(selectedIndex);
		//int pageID = Integer.parseInt(man.getPageIDs().get(selectedIndex));
		Log.i(AppConstants.TAG_STRING, "ListStories: Viewing story ID: " + s.getId());
		Intent i = new Intent(this, ViewStoryActivity.class);
		s.feedIntent(i);
		//i.putExtra("pageID", s.getId());
		//i.putExtra("name", s.getName());

		//i.putExtra("useAuthentication", useAuthentication());
		startActivity(i);
	}

	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
	}

	public void resetViewSourceExtra(ViewSource newViewSource) {
	}

	public ContentType getContentType() {
		return ContentType.stories; 
	}

	

}