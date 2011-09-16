package com.sofurry.services;

//~--- imports ----------------------------------------------------------------

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.activities.ListPMActivity;
import com.sofurry.base.interfaces.ICanHandleFeedback;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.requests.RequestHandler;
import com.sofurry.util.Authentication;
import com.sofurry.util.ProgressSignal;


//~--- classes ----------------------------------------------------------------

/**
 *
 *
 */
public class PmNotificationService
        extends WakefulIntentService
        implements ICanHandleFeedback {
    private static long lastCheck_ = 0;
    private SharedPreferences prefs;
    //~--- fields -------------------------------------------------------------

    private RequestHandler requestHandler_;


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
        // Load auth information from server
        Authentication.loadAuthenticationInformation(this);

        if (hasAuthInformation()) {
            Log.i(AppConstants.TAG_STRING, "Requesting PM count (Authorized)...");
            getRequestParameters().executeInline(getRequestHandler());
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
        if (id == AppConstants.REQUEST_ID_FETCHDATA) {
            int messageCount = obj.getInt("unreadpmcount");

            if (messageCount > 0) {
                NotificationManager manager;
                Notification        note;
                PendingIntent       pendingIntent;
                Intent              intent;

                // Get the notification manager system service
                manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                // Create a new notification
                note = new Notification(R.drawable.icon,
                                        "New SoFurry PM(s)",
                                        System.currentTimeMillis());

                // Create the Intent and wrap it in a PendingIntent
                intent = new Intent(this, ListPMActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                String message = "You have " + messageCount + " new unread message";
                if (messageCount > 1)
                	message += "s";

                message += ".";
                // Set some settings for the notification
                note.setLatestEventInfo(this,
                                        "SoFurry PM",
                                        message,
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

            // Set timestamp of when we last checked, for incremental message checks
            PmNotificationService.lastCheck_ = (System.currentTimeMillis() / 1000);
            prefs.edit().putLong(AppConstants.PREFERENCE_LAST_PM_CHECK_TIME, PmNotificationService.lastCheck_).commit();

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

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs         = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the last check time from preferences storage at startup
        PmNotificationService.lastCheck_ = prefs.getLong(AppConstants.PREFERENCE_LAST_PM_CHECK_TIME, 0);

    	
    	AjaxRequest req = new AjaxRequest();

        // Set request parameters
        req.addParameter("f", "unreadpmcount");
        req.addParameter("since", ""+PmNotificationService.lastCheck_);

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
