package com.sofurry.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.ListAdapter;

import com.sofurry.AbstractContentList;
import com.sofurry.R;
import com.sofurry.ViewPMActivity;
import com.sofurry.model.PrivateMessage;
import com.sofurry.requests.AjaxRequest;

public class ListPM extends AbstractContentList<PrivateMessage> {

	private ArrayList<String> pageIDs = new ArrayList<String>();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//TODO: PM Menu
		return false;
	}

	@Override
	public void resetViewSource(int newViewSource) {
		//This function is blank on purpose, since no view filter change is supported yet
	}

	@Override
	public AjaxRequest getFetchParameters(int page, int source) {
		AjaxRequest req = new AjaxRequest();

		req.addParameter("f", "pm");
		req.addParameter("page", "" + page);
		return req;
	}
	
	

	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentList#parseResponse(org.json.JSONObject)
	 * 
	 * Parses the Response for private messages
	 */
	@Override
	public void parseResponse(JSONObject obj) {
		try {
			JSONArray items = new JSONArray(obj.getString("items"));
			numResults = items.length();
			for (int i = 0; i < numResults; i++) {
				PrivateMessage m = new PrivateMessage();
				m.populate(items.getJSONObject(i));
				resultList.add(m);
				pageIDs.add("" + m.getId());
			}
		} catch (Exception e) {
			ronError(e);
		}
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
		Log.i("ListPM", "Viewing PM ID: " + pageID);
		Intent i = new Intent(this, ViewPMActivity.class);
		i.putExtra("PMID", pageID);
		startActivity(i);
	}

	public boolean useAuthentication() {
		return true;
	}

	@Override
	protected ListAdapter getAdapter(Context context) {
		return new PrivateMessageAdapter(context, R.layout.listitemtwolineicon, resultList);
	}


}
