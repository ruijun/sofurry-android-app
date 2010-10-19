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
import com.sofurry.itemviews.ViewJournalActivity;
import com.sofurry.model.Submission;
import com.sofurry.model.Submission.SUBMISSION_TYPE;
import com.sofurry.requests.AjaxRequest;

public class ListJournals extends AbstractContentList<Submission> {

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		return GalleryArt.createBrowse(page,source,man.getViewSearch(),AppConstants.CONTENTTYPE_JOURNALS,30);
	}

	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentList#parseResponse(org.json.JSONObject)
	 * Parses the response data from the JSONObject 
	 */
	@Override
	public void parseResponse(JSONObject obj) {
		try {
			GalleryArt.jsonToResultlist(obj, man, SUBMISSION_TYPE.JOURNAL);
//			JSONArray pagecontents = new JSONArray(obj.getString("pagecontents"));
//			JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
//			for (int i = 0; i < items.length(); i++) {
//				Submission s = new Submission();
//				s.populate(items.getJSONObject(i));
//				s.setType(SUBMISSION_TYPE.JOURNAL);
//				//s.loadUserIcon();
//
//				man.getResultList().add(s);
//				pageIDs.add("" + s.getId());
//			}
		} catch (Exception e) {
			man.onError(-1,e);
		}

		// Start downloading the thumbnails
		man.startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		Submission s = getDataItem(selectedIndex);
		//int pageID = Integer.parseInt(man.getPageIDs().get(selectedIndex));
		Log.i("ListJournals", "Viewing journal ID: " + s.getId());
		Intent i = new Intent(this, ViewJournalActivity.class);
		s.feedIntent(i);
//		i.putExtra("pageID", s.getId());
//		i.putExtra("name", s.getName());
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
