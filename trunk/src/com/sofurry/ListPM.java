package com.sofurry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.ListAdapter;

import com.sofurry.list.PrivateMessageAdapter;
import com.sofurry.model.PrivateMessage;

public class ListPM extends AbstractContentList<PrivateMessage> {

	@Override
	protected Map<String, String> getFetchParameters() {
		Map<String, String> kvPairs = new HashMap<String, String>();

		kvPairs.put("f", "pm");
		kvPairs.put("page", "0");
		return kvPairs;
	}

	@Override
	protected int parseResponse(String httpResult, ArrayList<PrivateMessage> list)
			throws JSONException {
		int numResults;
		String result;
		Log.i("PM.parseResponse", "response: "+httpResult);
		
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
		}
		return numResults;
	}

	@Override
	protected void setSelectedIndex(int selectedIndex) {
		
	}

	@Override
	protected boolean useAuthentication() {
		return true;
	}

	@Override 
	protected ListAdapter getAdapter(Context context) {
		return new PrivateMessageAdapter(context, R.layout.listitemtwolineicon, resultList);
	}

}
