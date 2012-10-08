package com.sofurry.base.classes;

import android.os.Bundle;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.model.Submission;

/**
 * @author Rangarig
 *
 * A class that contains all the tools for an item that can be faved and rated.
 *
 */
public abstract class SubmissionViewActivity
        extends ActivityWithRequests
        implements ICanCancel {
    protected String authorName;
    protected String name;
    protected String thumbURL;
    protected int    authorId;
    protected int    pageID;

    /**
     * Allows for the implementing view to add extra menu options to the menu
     * @param menu
     */
    public abstract void createExtraMenuOptions(Menu menu);

    /**
     * Method description
     *
     */
    @Override
    public void finish() {
        super.finish();
    }

    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            pageID     = extras.getInt("pageID");
            name       = extras.getString("name");
            authorId   = extras.getInt("authorId");
            authorName = extras.getString("authorName");
            thumbURL   = extras.getString("thumbnail");
        }
    }

    // changes currently viewed submission
    public void assignSubmission(Submission s) {
    	if (s != null) {
        	pageID = s.getId();
        	name = s.getName();
        	authorId = s.getAuthorID();
        	authorName = s.getAuthorName();
        	thumbURL = s.getThumbURL();
    	} else {
        	pageID = -1;
        	name = "";
        	authorId = -1;
        	authorName = "";
        	thumbURL = "";
    	}
    }
    
    /**
     * Creates the Context Menu for this Activity.
     *
     *
     * @param menu
     *
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        menu.add(0, AppConstants.MENU_SAVE, 0, "Save").setIcon(android.R.drawable.ic_menu_save);
        createExtraMenuOptions(menu);

        return result;
    }

    /**
     * Method description
     *
     *
     * @param id
     * @param e
     */
    @Override
    public void onError(Exception e) {
        pbh.hideProgressDialog();
        super.onError(e);
    }

    /**
     * Method description
     *
     *
     * @param keyCode
     * @param event
     *
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getRepeatCount() == 0)) {
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Method description
     *
     *
     * @param item
     *
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case AppConstants.MENU_SAVE:
            	if (pageID >= 0)
            		save();

                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Save current submission
     * Should check for current submission == null
     */
    public abstract void save();
}
