package com.sofurry.activities;

import android.database.Cursor;

import android.os.Bundle;

import android.view.View;

import android.widget.AutoCompleteTextView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.adapters.NamesAcDbAdapter;
import com.sofurry.base.classes.ActivityWithRequests;
import com.sofurry.requests.AjaxRequest;

import org.json.JSONObject;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 *
 * @author         SoFurry
 */
public class SendPMActivity
        extends ActivityWithRequests {
    private AutoCompleteTextView sendTo_;
    private NamesAcDbAdapter     dbHelper_;
    private String               msgSubject_;
    private String               toUserId_ = null;
    private String               toUserName_;
    private TextView             messageText_;
    private TextView             subject_;


    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param v
     */
    public void buttonClick(View v) {
        if (v.getId() == R.id.send) {
            if (sendTo_.getText().length() > 0) {
                AjaxRequest req = getSendParameters();

                pbh.showProgressDialog("Sending PM...");
                req.execute(requesthandler);
            } else {
                Toast.makeText(this, "You need to specify a recipient.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        Cursor cursor;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.writepm);

        // Find the needed views
        messageText_ = (TextView) findViewById(R.id.message_text);
        subject_     = (TextView) findViewById(R.id.subject);
        sendTo_      = (AutoCompleteTextView) findViewById(R.id.send_to);

        // Instantiate the DB helper object
        if (dbHelper_ == null) {
            dbHelper_ = new NamesAcDbAdapter(this).open();
        }

        // Fetch all the names as a Cursor and tell the activity to manage it
        cursor = dbHelper_.names_.getNames();

        startManagingCursor(cursor);

        // Create the CursorAdapter needed to bind data to the UI
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                                                              R.layout.nameac_list_item,
                                                              cursor,
                                                              new String[] { "name" },
                                                              new int[] { R.id.name });

        // Tell the system we want to use the 'name' column as our string representation
        adapter.setStringConversionColumn(1);

        // Finally, set the adapter for the AutoCompleteTextView field
        sendTo_.setAdapter(adapter);

        // Check if we have a saved instance
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                if (extras.containsKey("ToID")) {
                    toUserId_ = extras.getString("ToID");
                }

                if (extras.containsKey("ToName")) {
                    toUserName_ = extras.getString("ToName");

                    sendTo_.setText(toUserName_);
                }
            }
        } else {
            toUserId_   = (String) retrieveObject("toUserId");
            toUserName_ = (String) retrieveObject("toUserName");
            msgSubject_ = (String) retrieveObject("msgSubject");

            // Also set text fields with contents stores in the instance state object
            messageText_.setText((CharSequence) retrieveObject("messageText"));
            subject_.setText((CharSequence) retrieveObject("subject"));
            sendTo_.setText((CharSequence) retrieveObject("sendTo"));
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
        if (id == AppConstants.REQUEST_ID_SEND) {
            Boolean hadSuccess;

            // Hide progress dialog
            pbh.hideProgressDialog();

            // Figure out if we were successful in sending
            hadSuccess = obj.getBoolean("success");

            // Now act on it
            if (hadSuccess) {

                // Add the name to the name cache
                dbHelper_.names_.addName(sendTo_.getText().toString());

                // Display 'toast' message about the success
                Toast.makeText(this, "Message sent!", Toast.LENGTH_LONG).show();
                
                // Close the activity
                this.finish();
            } else {
                errorTitle_   = "Error";
                errorMessage_ = obj.getString("message");

                showDialog(AppConstants.DIALOG_ERROR_ID);
            }
        } else {
            super.onData(id, obj);    // Handle inherited events
        }
    }

    /**
     * Currently just used to make sure we at least -try- to clean up after ourselves in
     * regards to the database access
     *
     */
    @Override
    protected void onDestroy() {
        if (dbHelper_ != null) {
            dbHelper_.close();
        }

        super.onDestroy();
    }

    /**
     * Primarily happens when the phone is rotated, storing all the important data so it can be restored
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
        storeObject("toUserId", toUserId_);
        storeObject("toUserName", toUserName_);
        storeObject("msgSubject", msgSubject_);

        // And of course the various views
        storeObject("messageText", messageText_.getText());
        storeObject("subject", subject_.getText());
        storeObject("sendTo", sendTo_.getText());
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Prepares an AjaxRequest object for sending a PM
     *
     *
     * @return A filled-out AjaxRequest object, ready to be executed
     */
    protected AjaxRequest getSendParameters() {
        AjaxRequest req         = new AjaxRequest();
        String      messageText = messageText_.getText().toString();

        // Prepare message text, because line-breaks aren't transmitted correctly
        messageText = messageText.replaceAll("\n", "<br />").replaceAll("\r", "");

        // Set request parameters
        req.addParameter("f", "sendpm");
        req.addParameter("toUserName", sendTo_.getText().toString());
        req.addParameter("subject", subject_.getText().toString());
        req.addParameter("message", messageText);
        req.addParameter("parendId", "0");

        if ((toUserId_ != null) && (toUserId_.length() > 0)) {
            req.addParameter("toUserId", toUserId_);
        }

        // Set the request ID, so we know which request we're managing later
        req.setRequestID(AppConstants.REQUEST_ID_SEND);

        return req;
    }
}
