package com.sofurry.list;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.BaseAdapter;

import com.sofurry.AbstractContentList;
import com.sofurry.R;
import com.sofurry.itemviews.ViewPMActivity;
import com.sofurry.model.PrivateMessage;
import com.sofurry.requests.AjaxRequest;

public class ListPM extends AbstractContentList<PrivateMessage> {

	/* (non-Javadoc)
	 * @see com.sofurry.AbstractContentList#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Suppresses the default options menu. Which would not make a terrible lot of sense in this context.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
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
			for (int i = 0; i < items.length(); i++) {
				PrivateMessage m = new PrivateMessage();
				m.populate(items.getJSONObject(i));
				man.getResultList().add(m);
				//man.getPageIDs().add("" + m.getId());
			}
		} catch (Exception e) {
			man.ronError(e);
		}
	}

	@Override
	public void setSelectedIndex(int selectedIndex) {
		PrivateMessage pm = getDataItem(selectedIndex);
		//int pageID = Integer.parseInt(man.getPageIDs().get(selectedIndex));
		Log.i("ListPM", "Viewing PM ID: " + pm.getId());
		Intent i = new Intent(this, ViewPMActivity.class);
		i.putExtra("PMID", pm.getId());
		startActivity(i);
	}

	@Override
	public BaseAdapter getAdapter(Context context) {
		return new PrivateMessageAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
	}

	public void resetViewSourceExtra(int newViewSource) {
	}


}
