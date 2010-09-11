package com.sofurry.list;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListAdapter;

import com.sofurry.AbstractContentList;
import com.sofurry.R;
import com.sofurry.ViewStoryActivity;
import com.sofurry.model.Submission;
import com.sofurry.model.Submission.SUBMISSION_TYPE;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.Authentication;

public class ListJournals extends AbstractContentList<Submission> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		AjaxRequest req = new AjaxRequest();

		req.addParameter("f", "browse");
		req.addParameter("viewSource", ""+source);
		req.addParameter("contentType", "3");
		req.addParameter("entriesPerPage", "30");
		req.addParameter("page", "" + page);
		return req;
	}
	
	

	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentList#parseResponse(org.json.JSONObject)
	 * Parses the response data from the JSONObject 
	 */
	@Override
	public void parseResponse(JSONObject obj) {
		try {
			JSONArray pagecontents = new JSONArray(obj.getString("pagecontents"));
			JSONArray items = new JSONArray(pagecontents.getJSONObject(0).getString("items"));
			for (int i = 0; i < items.length(); i++) {
				Submission s = new Submission();
				s.populate(items.getJSONObject(i));
				s.setType(SUBMISSION_TYPE.JOURNAL);
				//s.loadUserIcon();

				resultList.add(s);
				pageIDs.add("" + s.getId());
			}
		} catch (Exception e) {
			ronError(e);
		}

		// Start downloading the thumbnails
		startThumbnailDownloader();
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		stopThumbDownloader();
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("ListJournals", "Viewing journal ID: " + pageID);
		Intent i = new Intent(this, ViewStoryActivity.class);
		i.putExtra("pageID", pageID);
		i.putExtra("useAuthentication", useAuthentication());
		startActivity(i);
		
	}

	public boolean useAuthentication() {
		return (Authentication.getUsername() != null && Authentication.getUsername().trim().length() > 0);
	}

	@Override
	protected ListAdapter getAdapter(Context context) {
		return new SubmissionListAdapter(context, R.layout.listitemtwolineicon, resultList);
	}

	@Override
	public void resetViewSource(int newViewSource) {
		pageIDs = new ArrayList<String>();
		super.resetViewSource(newViewSource);
	}

}
