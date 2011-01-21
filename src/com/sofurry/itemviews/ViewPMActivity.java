package com.sofurry.itemviews;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.base.classes.ActivityWithRequests;
import com.sofurry.requests.AjaxRequest;


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
     * @param contents
     *
     * @return
     */
    protected String formatContents(String contents) {
        StringBuilder contentBuilder;

        contents = contents.replaceAll(AppConstants.PM_CONTENTS_URL_REGEX, AppConstants.PM_CONTENTS_URL_TEMPLATE);

        // Create object
        contentBuilder = new StringBuilder(contents);

        // Insert prefix and postfix information to get the right colour
        contentBuilder.insert(0, AppConstants.PM_CONTENTS_PREFIX);
        contentBuilder.append(AppConstants.PM_CONTENTS_POSTFIX);
        
        // Return result, replacing line breaks
        return contentBuilder.toString().replace("\n", "<br/>");
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
     * @param id Request ID
     * @param obj Parsed JSON object
     *
     * @throws Exception
     */
    @Override
    public void onData(int id, JSONObject obj) throws Exception {
        if (id == AppConstants.REQUEST_ID_FETCHCONTENT) {
            JSONArray  items;
            JSONObject jsonItem;

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
            content_ = formatContents(jsonItem.getString("message"));

            showContent();

            // Also set the subject if need be
            if (subject_.getText().length() == 0) {

                // If there's no 'Re:' in front of the subject, add one
                if (!msgSubject_.substring(0, 3).equalsIgnoreCase("re:")) {
                    subject_.setText("Re: " + msgSubject_);
                } else {
                    subject_.setText(msgSubject_);
                }
            }
        } else if (id == AppConstants.REQUEST_ID_SEND) {
        	Boolean hadSuccess;
        	
            // Hide progress dialog
            pbh.hideProgressDialog();
            
            // Figure out if we were successful in sending
            hadSuccess = obj.getBoolean("success");
            
            // Now act on it
            if (hadSuccess) {
            	Toast.makeText(this, "Message sent!", Toast.LENGTH_LONG).show();
            	this.finish();
            } else {
            	// TODO: Make it display a dialog here
            	Toast.makeText(this, "Message sending failed.", Toast.LENGTH_LONG).show();
            }
        } else {
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
        // Starting with returned data
        storeObject("content", content_);
        storeObject("fromUserId", fromUserId_);
        storeObject("fromUserName", fromUserName_);
        storeObject("msgDate", msgDate_);
        storeObject("msgSubject", msgSubject_);
        storeObject("pmId", (Integer) pmId_);

        // And the state of the drawer
        storeObject("drawerOpened", (Boolean) replyDrawer_.isOpened());

        // And of course the various views
        storeObject("messageText", messageText_.getText());
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

        // Set request parameters
        req.addParameter("f", "pmcontent");
        req.addParameter("id", "" + id);
        
        // Set the request ID, so we know which request we're managing later
        req.setRequestID(AppConstants.REQUEST_ID_FETCHCONTENT);

        return req;
    }

    /**
     * Prepares an AjaxRequest object for sending a PM reply
     *
     *
     * @return A filled-out AjaxRequest object, ready to be executed
     */
    protected AjaxRequest getReplyParameters() {
        AjaxRequest req = new AjaxRequest();
        String messageText = messageText_.getText().toString();
        
        // Prepare message text, because line-breaks aren't transmitted correctly
        messageText = messageText.replaceAll("\n", "<br />").replaceAll("\r", "");

        // Set request parameters
        req.addParameter("f", "sendpm");
        req.addParameter("toUserId", fromUserId_);
        req.addParameter("toUserName", fromUserName_);
        req.addParameter("parentId", "" + pmId_);
        req.addParameter("subject", subject_.getText().toString());
        req.addParameter("message", messageText);
        
        // Set the request ID, so we know which request we're managing later
        req.setRequestID(AppConstants.REQUEST_ID_SEND);

        return req;
    }
}
