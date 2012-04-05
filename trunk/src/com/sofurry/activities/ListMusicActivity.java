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

public class ListMusicActivity extends AbstractContentList<Submission> {

	public Request getFetchParameters(int page, String search) throws Exception {
		Request req = ApiFactory.createBrowse(man.getViewSource(),search,ContentType.music,AppConstants.ENTRIESPERPAGE_GALLERY,page);
		return req;
	}

	@Override
	public void parseResponse(JSONObject obj) throws Exception {
		GalleryArtActivity.jsonToResultlist(obj, man);
		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		Submission s = getDataItem(selectedIndex);
		Log.i(AppConstants.TAG_STRING, "ListMusic: Viewing music ID: " + s.getId());
		Intent i = new Intent(this, ViewMusicActivity.class);
		s.feedIntent(i);
		//i.putExtra("pageID", s.getId());
		//i.putExtra("name", s.getName());

//		i.putExtra("username", s.getAuthorName());
//		i.putExtra("name", s.getName());
		startActivity(i);
	}

	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
	}

	public void resetViewSourceExtra(ViewSource newViewSource) {
	}


	@Override
	public void finish() {
		
		super.finish();
	}

	/* (non-Javadoc)
	 * @see com.sofurry.base.interfaces.IManagedActivity#getContentType()
	 */
	public ContentType getContentType() {
		return ContentType.music;
	}

	
	

}