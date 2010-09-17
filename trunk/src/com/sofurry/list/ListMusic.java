package com.sofurry.list;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;

import com.sofurry.AbstractContentList;
import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.gallery.GalleryArt;
import com.sofurry.itemviews.ViewMusicActivity;
import com.sofurry.model.Submission;
import com.sofurry.model.Submission.SUBMISSION_TYPE;
import com.sofurry.requests.AjaxRequest;

public class ListMusic extends AbstractContentList<Submission> {

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		return GalleryArt.createBrowse(page,source,man.getViewSearch(),AppConstants.CONTENTTYPE_MUSIC,30);
	}
	

	@Override
	public void parseResponse(JSONObject obj) throws Exception {
		GalleryArt.jsonToResultlist(obj, man, SUBMISSION_TYPE.MUSIC);
		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		Submission s = getDataItem(selectedIndex);
		Log.i("ListMusic", "Viewing music ID: " + s.getId());
		Intent i = new Intent(this, ViewMusicActivity.class);
		i.putExtra("pageID", s.getId());
		i.putExtra("name", s.getName());

//		i.putExtra("username", s.getAuthorName());
//		i.putExtra("name", s.getName());
		startActivity(i);
	}

	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
	}

	public void resetViewSourceExtra(int newViewSource) {
	}


	@Override
	public void finish() {
		
		super.finish();
	}

	
	

}