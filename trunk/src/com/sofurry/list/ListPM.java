package com.sofurry.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListAdapter;

import com.sofurry.AbstractContentList;
import com.sofurry.ContentController;
import com.sofurry.R;
import com.sofurry.ViewPMActivity;
import com.sofurry.model.PrivateMessage;

public class ListPM extends AbstractContentList<PrivateMessage> implements ContentController<PrivateMessage> {

	private ArrayList<String> pageIDs;

	@Override
	protected Map<String, String> getFetchParameters(int page) {
		Map<String, String> kvPairs = new HashMap<String, String>();

		kvPairs.put("f", "pm");
		kvPairs.put("page", "" + page);
		return kvPairs;
	}

	public int parseResponse(String httpResult, ArrayList<PrivateMessage> list) throws JSONException {
		int numResults;
		String result;
		Log.i("PM.parseResponse", "response: " + httpResult);
		pageIDs = new ArrayList<String>();
		if (currentPage > 0) {
			PrivateMessage prev = new PrivateMessage();
			prev.setSubject("previous page ("+(currentPage)+")");
			list.add(prev);
			pageIDs.add("prev");
		}

		JSONObject jsonParser = new JSONObject(httpResult);
		JSONArray items = new JSONArray(jsonParser.getString("items"));
		numResults = items.length();
		for (int i = 0; i < numResults; i++) {
			JSONObject jsonItem = items.getJSONObject(i);
			String id = jsonItem.getString("id");
			String fromUserName = jsonItem.getString("fromUserName");
			String date = jsonItem.getString("date");
			String subject = jsonItem.getString("subject");
			String status = jsonItem.getString("status");
			PrivateMessage m = new PrivateMessage();
			m.setFromUser(fromUserName);
			m.setId(Integer.parseInt(id));
			m.setDate(date);
			m.setSubject(subject);
			m.setStatus(status);

			list.add(m);
			pageIDs.add(id);
		}

		PrivateMessage next = new PrivateMessage();
		next.setSubject("next page ("+(currentPage+2)+")");
		list.add(next);
		pageIDs.add("next");

		return numResults;
	}

	@Override
	protected void setSelectedIndex(int selectedIndex) {
		if (pageIDs.get(selectedIndex).equalsIgnoreCase("next")) {
			currentPage++;
			loadPage(currentPage);
		} else if (pageIDs.get(selectedIndex).equalsIgnoreCase("prev")) {
			if (currentPage > 0) {
				currentPage--;
				loadPage(currentPage);
			}
		} else {
			int pageID = Integer.parseInt(pageIDs.get(selectedIndex));
			Log.i("ListPM", "Viewing PM ID: " + pageID);
			Intent i = new Intent(this, ViewPMActivity.class);
			i.putExtra("PMID", pageID);
			startActivity(i);
		}
	}

	public boolean useAuthentication() {
		return true;
	}

	@Override
	protected ListAdapter getAdapter(Context context) {
		return new PrivateMessageAdapter(context, R.layout.listitemtwolineicon, resultList);
	}

}
