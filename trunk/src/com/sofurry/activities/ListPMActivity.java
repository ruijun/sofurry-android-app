package com.sofurry.activities;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.adapters.PrivateMessageAdapter;
import com.sofurry.base.classes.AbstractContentList;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.model.PrivateMessage;


/**
 * Class description
 *
 *
 * @author SoFurry
 */
/**
 * @author f034561
 *
 */
public class ListPMActivity extends AbstractContentList<PrivateMessage> {

    /**
     * Suppresses the default options menu, and replaces it with one that makes
     * more sense for this context.
     *
     *
     * @param menu
     *
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.listpmmenu, menu);

        return true;
    }

    /**
     * Method description
     *
     *
     * @param item
     *
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.compose:
                intent = new Intent(this, SendPMActivity.class);

                startActivity(intent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Parses the Response for private messages
     *
     *
     * @param obj
     */
    @Override
    public void parseResponse(JSONObject obj) {
        try {
            JSONArray items = new JSONArray(obj.getString("items"));

            for (int i = 0; i < items.length(); i++) {
                PrivateMessage m = new PrivateMessage();

                m.populate(items.getJSONObject(i));
                man.getResultList().add(m);

                // man.getPageIDs().add("" + m.getId());
            }
        } catch (Exception e) {
            man.onError(e);
        }
    }

    /**
     * Method description
     *
     * @param newViewSource
     */
    public void resetViewSourceExtra(ViewSource newViewSource) {}

    /**
     * Method description
     *
     *
     * @param context
     *
     * @return
     */
    @Override
    public BaseAdapter getAdapter(Context context) {
        return new PrivateMessageAdapter(context, R.layout.listitemtwolineicon, man.getResultList());
    }

    /* (non-Javadoc)
     * @see com.sofurry.base.classes.AbstractContentList#getFetchRequest(int)
     * Overrides the getFetchRequest method, so this call is not handled
     * by the activity manager (which usually happens for the media oriented lists)
     */
    public Request getFetchRequest(int page) throws Exception {
    	return ApiFactory.createListPMs(page, AppConstants.ENTRIESPERPAGE_LIST);
    }

    /**
     * Method description
     *
     *
     * @param selectedIndex
     */
    @Override
    public void setSelectedIndex(int selectedIndex) {
        PrivateMessage pm = getDataItem(selectedIndex);

        // int pageID = Integer.parseInt(man.getPageIDs().get(selectedIndex));
        Log.i(AppConstants.TAG_STRING, "ListPM: Viewing PM ID: " + pm.getId());

        Intent intent = new Intent(this, ViewPMActivity.class);
       
        intent.putExtra("PMID", pm.getId());
        
        startActivity(intent);

        if ((AppConstants.PM_STATUS_NEW+"").equals(pm.getStatus())) {
        	pm.setStatus(""+AppConstants.PM_STATUS_READ);
        	updateView();
        }
    }

	public ContentType getContentType() {
		return null;
	}
}
