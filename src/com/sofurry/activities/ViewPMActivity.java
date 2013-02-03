package com.sofurry.activities;

import android.content.Context;

import android.os.Bundle;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.webkit.WebView;

import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.adapters.NamesAcDbAdapter;
import com.sofurry.base.classes.ActivityWithRequests;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.requests.AndroidRequestWrapper;
import com.sofurry.requests.DataCall;

import org.json.JSONArray;
import org.json.JSONObject;


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

    public void buttonClick(View v) {
    	try {
            if (v.getId() == R.id.reply) {
                pbh.showProgressDialog("Sending reply...");
                
                String      messageText = messageText_.getText().toString();
                // Prepare message text, because line-breaks aren't transmitted correctly
                messageText = messageText.replaceAll("\n", "<br />").replaceAll("\r", "");

                Request req = ApiFactory.createSendPM(fromUserName_,Integer.parseInt(fromUserId_),subject_.getText().toString(),messageText,pmId_);
        		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
        		arw.exec(new DataCall() { public void call() { handlePMContent((JSONObject)arg1);	} });
                
            }
		} catch (Exception e) {
			onError(e);
		}
    }

    protected String formatContents(String contents, String fromUserName, String subject, String date) {
        StringBuilder contentBuilder;

        contents = contents.replaceAll(AppConstants.PM_CONTENTS_URL_REGEX, AppConstants.PM_CONTENTS_URL_TEMPLATE);
        contents = contents.replace("\n", "<br/>");
        // Create object
        contentBuilder = new StringBuilder();

        // Insert prefix and postfix information to get the right colour
        contentBuilder.append(AppConstants.PM_CONTENTS_PREFIX);
        contentBuilder.append("<b>From:</b> </i>");
        contentBuilder.append(fromUserName);
        contentBuilder.append("</i><br/>");
        contentBuilder.append("<b>Subject:</b> </i>");
        contentBuilder.append(subject);
        contentBuilder.append("</i><br/>");
        contentBuilder.append("<b>Date:</b> </i>");
        contentBuilder.append(date);
        contentBuilder.append("</i><br/><br/>");
        contentBuilder.append(contents);
        contentBuilder.append(AppConstants.PM_CONTENTS_POSTFIX);
        
        // Return result, replacing line breaks
        return contentBuilder.toString();
    }

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

                pbh.showProgressDialog("Fetching data...");

    			Request req = ApiFactory.createGetPmContent(pmId_);
        		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
        		arw.exec(new DataCall() { public void call() { handlePMContent((JSONObject)arg1);	} });

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
     * Handles the feedback data from fetch PM Content
     * @param obj
     */
    public void handlePMContent(JSONObject obj) {
    	try {
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
            content_ = formatContents(jsonItem.getString("message"), fromUserName_, msgSubject_, msgDate_);

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
		} catch (Exception e) {
			onError(e);
		}
    }
    
    /**
     * Handles the date Returned from SendPM
     * @param obj
     */
    public void handleSendData(JSONObject obj) {
    	try {
            Boolean hadSuccess;

            // Hide progress dialog
            pbh.hideProgressDialog();

            // Figure out if we were successful in sending
            hadSuccess = obj.getBoolean("success");

            // Now act on it
            if (hadSuccess) {
            	
            	// Add the name to the name cache, because well.. Why not?
            	NamesAcDbAdapter dbHelper = new NamesAcDbAdapter(this).open();
            	dbHelper.names_.addName(fromUserName_);
            	dbHelper.close();

                // Display 'toast' message about the success
                Toast.makeText(this, "Message sent!", Toast.LENGTH_LONG).show();

                // Close the activity
                this.finish();
            } else {
                errorTitle_   = "Error";
                errorMessage_ = obj.getString("message");

                showDialog(AppConstants.DIALOG_ERROR_ID);
            }
		} catch (Exception e) {
			onError(e);
		}
    }

    /**
     * Changes the drawer's handle graphics, as well as closes the soft keyboard
     *
     */
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

}
