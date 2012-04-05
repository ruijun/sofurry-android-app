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

public class ListJournalsActivity extends AbstractContentList<Submission> {

	public Request getFetchParameters(int page, String search) throws Exception {
		Request req = ApiFactory.createBrowse(man.getViewSource(),search,ContentType.art,AppConstants.ENTRIESPERPAGE_GALLERY,page);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentList#parseResponse(org.json.JSONObject)
	 * Parses the response data from the JSONObject 
	 */
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
		Log.i(AppConstants.TAG_STRING, "ListJournals: Viewing journal ID: " + s.getId());
		Intent i = new Intent(this, ViewJournalActivity.class);
		s.feedIntent(i);
		startActivity(i);
		
	}

	@Override
	public BaseAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
	}

	
	
	public void resetViewSourceExtra(ViewSource newViewSource) {
	}

	/* (non-Javadoc)
	 * @see com.sofurry.base.interfaces.IManagedActivity#getContentType()
	 */
	public ContentType getContentType() {
		return ContentType.journals;
	}

}
