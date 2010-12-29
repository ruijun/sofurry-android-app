package com.sofurry.itemviews;

import android.content.Context;

import android.os.Bundle;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.webkit.WebView;

import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.base.classes.ActivityWithRequests;
import com.sofurry.requests.AjaxRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;


/**
 * Class description
 *
 *
 * @author Author Name
 */
public class ViewPMActivity
        extends ActivityWithRequests
        implements SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener {
    private ImageView     drawerHandle_;
    private SlidingDrawer replyDrawer_;
    private String        content_ = null;
    private String        fromUserId_;
    private String        fromUserName_;
    private String        msgDate_;
    private String        msgSubject_;
    private TextView      messageText_;
    private TextView      subject_;
    private WebView       webview_;
    private int           pmId_;


    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param v
     */

    /**
     * @param v
     */
    public void buttonClick(View v) {
        if (v.getId() == R.id.reply) {
            AjaxRequest req = getReplyParameters();

            pbh.showProgressDialog("Sending reply...");
            req.execute(requesthandler);
        }
    }

    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpm);

        // Find the needed views
        drawerHandle_ = (ImageView) findViewById(R.id.handle);
        messageText_  = (TextView) findViewById(R.id.message_text);
        replyDrawer_  = (SlidingDrawer) findViewById(R.id.drawer);
        subject_      = (TextView) findViewById(R.id.subject);
        webview_      = (WebView) findViewById(R.id.browser);

        // Set the WebView's background colour to black
        webview_.setBackgroundColor(0);

        // Attach SlidingDrawer listeners
        replyDrawer_.setOnDrawerOpenListener(this);
        replyDrawer_.setOnDrawerCloseListener(this);

        // Check if we have a saved instance
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                pmId_ = extras.getInt("PMID");

                AjaxRequest req = getFetchParameters(pmId_);

                pbh.showProgressDialog("Fetching data...");
                req.execute(requesthandler);
            }
        } else {
            boolean drawerOpened;

            // Retrieve objects stored in the instance state object
            content_      = (String) retrieveObject("content");
            drawerOpened  = (Boolean) retrieveObject("drawerOpened");
            fromUserId_   = (String) retrieveObject("fromUserId");
            fromUserName_ = (String) retrieveObject("fromUserName");
            msgDate_      = (String) retrieveObject("msgDate");
            msgSubject_   = (String) retrieveObject("msgSubject");
            pmId_         = (Integer) retrieveObject("pmId");

            // Also set text fields with contents stores in the instance state object
            messageText_.setText((CharSequence) retrieveObject("messageText"));
            subject_.setText((CharSequence) retrieveObject("subject"));

            // Open the drawer if it was open when the instance was saved
            if (drawerOpened) {
                replyDrawer_.open();
            }

            // Display the contents
            showContent();
        }
    }

    /**
     * Method description
     *
     *
     * @param id
     * @param obj
     *
     * @throws Exception
     */
    /* (non-Javadoc)
     * @see com.sofurry.base.classes.ActivityWithRequests#onData(int, org.json.JSONObject)
     */
    @Override
    public void onData(int id, JSONObject obj) throws Exception {
        if (id == AppConstants.REQUEST_ID_FETCHCONTENT) {
            JSONArray     items;
            JSONObject    jsonItem;
            StringBuilder contentBuilder;

            // Hide the progress dialog
            pbh.hideProgressDialog();

            // Parse message from retrieved array
            items    = new JSONArray(obj.getString("items"));
            jsonItem = items.getJSONObject(0);

            // Retrieve the data we need to use later
            msgSubject_   = jsonItem.getString("subject");
            fromUserId_   = jsonItem.getString("fromUserId");
            fromUserName_ = jsonItem.getString("fromUserName");
            msgDate_      = jsonItem.getString("date");

            // Generate and show contents
            contentBuilder = new StringBuilder(jsonItem.getString("message"));

            contentBuilder.insert(0, AppConstants.PM_CONTENTS_PREFIX);
            contentBuilder.append(AppConstants.PM_CONTENTS_POSTFIX);

            content_ = contentBuilder.toString().replace("\n", "<br/>");

            showContent();
        } else if (id == AppConstants.REQUEST_ID_SEND) {
        	// Hide progress dialog
        	pbh.hideProgressDialog();
        }
        else {
            super.onData(id, obj);    // Handle inherited events
        }
    }

    /**
     * Changes the drawer's handle graphics, as well as closes the soft keyboard
     *
     */
    @Override
    public void onDrawerClosed() {
        if (isOrientationLandscape()) {
            drawerHandle_.setImageResource(R.drawable.reply_land);
        } else {
            drawerHandle_.setImageResource(R.drawable.reply);
        }

        // Hide the soft keyboard, if it's visible
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
            subject_.getWindowToken(),
            0);
    }

    /**
     * Changes the drawer's handle graphics
     *
     */
    @Override
    public void onDrawerOpened() {
        if (isOrientationLandscape()) {
            drawerHandle_.setImageResource(R.drawable.reply_land_close);
        } else {
            drawerHandle_.setImageResource(R.drawable.reply_close);
        }
    }

    /**
     * Primarily when the phone is rotated, storing all the important data so it can be restored
     * and a proper user experience is ensured
     *
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save various contents so we can restore it again
        storeObject("content", content_);
        storeObject("drawerOpened", (Boolean) replyDrawer_.isOpened());
        storeObject("fromUserId", fromUserId_);
        storeObject("fromUserName", fromUserName_);
        storeObject("messageText", messageText_.getText());
        storeObject("msgDate", msgDate_);
        storeObject("msgSubject", msgSubject_);
        storeObject("pmId", (Integer) pmId_);
        storeObject("subject", subject_.getText());
    }

    /**
     * Displays the contents in the WebView
     */
    public void showContent() {
        webview_.loadData(content_, "text/html", "utf-8");
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Prepares the parameters used to fetch a PM from the server
     *
     *
     * @param id The ID of the PM in question
     *
     * @return A filled-out AjaxRequest object, ready to be executed
     */
    protected AjaxRequest getFetchParameters(int id) {
        AjaxRequest req = new AjaxRequest();

        req.addParameter("f", "pmcontent");
        req.addParameter("id", "" + id);
        req.setRequestID(AppConstants.REQUEST_ID_FETCHCONTENT);

        return req;
    }

    /**
     * Prepares an AjaxRequest object for sending a PM reply
     *
     *
     * @return A filled-out AjaxRequest object, ready to be executed
     */
    /**
     * @return
     */
    protected AjaxRequest getReplyParameters() {
        AjaxRequest req = new AjaxRequest();

        req.addParameter("f", "sendpm");
        req.addParameter("toUserId", fromUserId_);
        req.addParameter("toUserName", fromUserName_);
        req.addParameter("parentId", "" + pmId_);

        // Since UTF-8 should always be supported, there shouldn't be an issue here.
        // It still requires a try-statement, though.
        try {
            req.addParameter("subject", URLEncoder.encode(subject_.getText().toString(), "UTF-8"));
            req.addParameter("message", URLEncoder.encode(messageText_.getText().toString(), "UTF-8"));
        } catch (Exception exception) {}

        req.setRequestID(AppConstants.REQUEST_ID_SEND);

        return req;
    }
}
