package com.sofurry.list;

import android.content.Context;
import android.content.Intent;

import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.BaseAdapter;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.base.classes.AbstractContentList;
import com.sofurry.itemviews.ViewPMActivity;
import com.sofurry.listadapters.PrivateMessageAdapter;
import com.sofurry.model.PrivateMessage;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.submits.SendPMActivity;

import org.json.JSONArray;
import org.json.JSONObject;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 *
 * @author SoFurry
 */
public class ListPM
        extends AbstractContentList<PrivateMessage> {

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
            man.onError(-1, e);
        }
    }

    /**
     * Method description
     *
     *
     * @param newViewSource
     */
    public void resetViewSourceExtra(int newViewSource) {}

    //~--- get methods --------------------------------------------------------

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

    /**
     * Method description
     *
     *
     * @param page
     * @param source
     *
     * @return
     */
    @Override
    public AjaxRequest getFetchParameters(int page, int source) {
        AjaxRequest req = new AjaxRequest();

        req.addParameter("f", "pm");
        req.addParameter("page", "" + page);

        return req;
    }

    //~--- set methods --------------------------------------------------------

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
}
