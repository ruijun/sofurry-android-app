package com.sofurry.services;

//~--- imports ----------------------------------------------------------------

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Intent;

import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.ICanHandleFeedback;
import com.sofurry.list.ListPM;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.ProgressSignal;
import com.sofurry.requests.RequestHandler;
import com.sofurry.requests.RequestThread;
import com.sofurry.util.Authentication;

import org.json.JSONObject;


//~--- classes ----------------------------------------------------------------

/**
 *
 *
 */
public class PmNotificationService
        extends WakefulIntentService
        implements ICanHandleFeedback {
    private RequestHandler requestHandler_;
    private int            messageCount_ = -1;


    //~--- constructors -------------------------------------------------------

    // private long uniqueStorageKey_;

    /**
     * Constructs the PM Notification Service
     *
     */
    public PmNotificationService() {
        super("PmNotificationService");

        // uniqueStorageKey_ = System.nanoTime();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * The actual work-horse of the Service. It will start checking for new
     * private messages on the server
     *
     *
     * @param intent
     */
    @Override
    protected void doWakefulWork(Intent intent) {
        RequestThread thread = null;

        Log.i(AppConstants.TAG_STRING, "Requesting PM count...");

        // Load auth information from server
        Authentication.loadAuthenticationInformation(this);

        if (hasAuthInformation()) {
            thread = getRequestParameters().execute(getRequestHandler());

            try {
                thread.join();
            } catch (InterruptedException ignored) {}
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
    @Override
    public void onData(int id, JSONObject obj) throws Exception {
        // TODO Handle data
        Log.i(AppConstants.TAG_STRING,
              "onData called with ID: " + id + " :: Should be: " + AppConstants.REQUEST_ID_FETCHDATA);

        if (id == AppConstants.REQUEST_ID_FETCHDATA) {
            int messageCount = obj.getInt("unreadpmcount");

            // Check if they're the same
            Log.i(AppConstants.TAG_STRING, "Comparing " + messageCount_ + " to " + messageCount);

            if ((messageCount_ != messageCount) && (messageCount > 0)) {
                NotificationManager manager;
                Notification        note;
                PendingIntent       pendingIntent;
                Intent              intent;

                // Get the notification manager system service
                manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                // Create a new notification
                note = new Notification(android.R.drawable.stat_notify_voicemail,
                                        "New SoFurry PM(s)",
                                        System.currentTimeMillis());

                // Create the Intent and wrap it in a PendingIntent
                intent = new Intent(this, ListPM.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                // Set some settings for the notification
                note.setLatestEventInfo(this,
                                        "SoFurry PM",
                                        "You have " + messageCount + " unread message(s).",
                                        pendingIntent);

                note.vibrate  = AppConstants.VIBRATE_PM_INCOMING;
                note.ledARGB  = 0x0000FFFF;    // Cyan
                note.ledOffMS = 1500;
                note.ledOnMS  = 500;
                note.number   = messageCount;
                note.flags    = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;

                // Fire off the notification
                manager.notify(AppConstants.NOTIFICATION_ID_PM, note);
            }

            // Set messageCount_ to the current value for comparison next time
            messageCount_ = messageCount;
        }
    }

    /**
     * Method description
     *
     *
     * @param id
     * @param e
     */
    @Override
    public void onError(int id, Exception e) {
        Log.e(AppConstants.TAG_STRING, "Error occurred: " + e.getLocalizedMessage());
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
    @Override
    public void onOther(int id, Object obj) throws Exception {
        Log.w(AppConstants.TAG_STRING, "Unexpected object type: " + obj.getClass().getName() + " received");
    }

    /**
     * Method description
     *
     *
     * @param id
     * @param prg
     *
     * @throws Exception
     */
    @Override
    public void onProgress(int id, ProgressSignal prg) throws Exception {
        // Might place some logging stuff here if needed...
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Override
    public void refresh() throws Exception {
        // Nothing to do here
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    protected RequestHandler getRequestHandler() {
        if ((requestHandler_ == null) || (requestHandler_.isKilled())) {
            requestHandler_ = new RequestHandler(this);
        }

        return requestHandler_;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected AjaxRequest getRequestParameters() {
        AjaxRequest req = new AjaxRequest();

        // Set request parameters
        req.addParameter("f", "unreadpmcount");

        // Set request ID
        req.setRequestID(AppConstants.REQUEST_ID_FETCHDATA);

        // Return result
        return req;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected boolean hasAuthInformation() {
        if ((Authentication.getUsername() == null) || (Authentication.getUsername().trim().length() <= 0)
                || (Authentication.getPassword() == null) || (Authentication.getPassword().trim().length() <= 0)) {
            return false;
        }

        return true;
    }
}
