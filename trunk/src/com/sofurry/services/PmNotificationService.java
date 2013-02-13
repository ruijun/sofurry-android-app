package com.sofurry.services;


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
import com.sofurry.activities.SFBrowsePMActivity;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.core.AuthenticationHandler;
import com.sofurry.mobileapi.core.Request;

public class PmNotificationService extends WakefulIntentService {
    private static long lastCheck_ = 0;
    private SharedPreferences prefs;


    /**
     * Constructs the PM Notification Service
     *
     */
    public PmNotificationService() {
        super("PmNotificationService");
    }

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
//    	if (!hasAuthInformation()) // Then the authentification handler is already initialized, we do not need to do that again.
        try {
			if (! AuthenticationHandler.useAuthentication(this)) // Then the authentification handler is already initialized, we do not need to do that again.
			  AuthenticationHandler.loadAuthenticationInformation(this);

//	        if (hasAuthInformation()) {
	        if (AuthenticationHandler.useAuthentication(this)) {
	            Log.i(AppConstants.TAG_STRING, "Requesting PM count (Authorized)...");
	            
	            handleUnreadPMData(createRequest().execute());
	        }
		} catch (Exception e) {
			e.printStackTrace();
	        Log.e(AppConstants.TAG_STRING, "Error occurred: " + e.getLocalizedMessage());
		} 
    }

    /**
     * Handles the data returned by UnreadPmCount
     * @param obj
     * @throws Exception
     */
    public void handleUnreadPMData(JSONObject obj) throws Exception {
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
//            intent = new Intent(this, ListPMActivity.class);
            intent = new Intent(this, SFBrowsePMActivity.class);

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

    protected Request createRequest() {

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs         = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the last check time from preferences storage at startup
        PmNotificationService.lastCheck_ = prefs.getLong(AppConstants.PREFERENCE_LAST_PM_CHECK_TIME, 0);

    	return ApiFactory.createUnreadPMCount(PmNotificationService.lastCheck_);
    }

/*    protected boolean hasAuthInformation() {
        if ((AuthenticationHandler.getUsername() == null) || (AuthenticationHandler.getUsername().trim().length() <= 0)
                || (AuthenticationHandler.getPassword() == null) || (AuthenticationHandler.getPassword().trim().length() <= 0)) {
            return false;
        }

        return true;
    }/**/

}
