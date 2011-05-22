package com.sofurry.util;

//~--- imports ----------------------------------------------------------------

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

import android.os.SystemClock;

import android.preference.PreferenceManager;

import com.sofurry.AppConstants;
import com.sofurry.receivers.OnAlarmReceiver;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 */
public class BootVersionChecker {
    /**
     * Method description
     *
     *
     * @param context
     */
    public static void scheduleAlarm(Context context) {
        BootVersionChecker.scheduleAlarm(context, false);
    }

    /**
     * Method description
     *
     *
     * @param context
     * @param onlyIfNotLaunched
     */
    public static void scheduleAlarm(Context context, boolean onlyIfNotLaunched) {
        BootVersionChecker bvc           = new BootVersionChecker();
        AlarmManager       manager       = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent             alarmIntent   = new Intent(context, OnAlarmReceiver.class);
        PendingIntent      pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        SharedPreferences  prefs         = PreferenceManager.getDefaultSharedPreferences(context);
        long               interval;

        // It hasn't, so let's launch one.
        // But first we'll be sure to cancel old ones, just in case.
        manager.cancel(pendingIntent);

        // Retrieve the interval, which is stored in preferences as a string
        try {
            interval = Long.parseLong(prefs.getString(AppConstants.PREFERENCE_PM_CHECK_INTERVAL,
                                                      Long.toString(AppConstants.ALARM_CHECK_DELAY_PERIOD)));
        } catch (NumberFormatException e) {
            interval = AppConstants.ALARM_CHECK_DELAY_PERIOD;
        }

        // Then start schedule a new one
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                             SystemClock.elapsedRealtime() + AppConstants.ALARM_CHECK_DELAY_FIRST,
                             interval,
                             pendingIntent);

        // Tell the system about this newly set alarm
        bvc.setHasLaunched(context);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param context
     *
     * @return
     */
    public int getVersionCode(Context context) {
        int versionCode = 0;

        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName,
                                                                     0).versionCode;
        } catch (NameNotFoundException e) {
            // Do nothing, as this really shouldn't happen
        }

        return versionCode;
    }

    /**
     * Method description
     *
     *
     *
     * @param context
     * @return
     */
    public boolean hasLaunched(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Check if the preference value matches the version code
        if (prefs.getInt(AppConstants.PREFERENCE_LAST_LAUNCH_VERSION, 0) != getVersionCode(context)) {
            return false;
        }

        // We have already launched the alarm
        return true;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param context
     */
    public void setHasLaunched(Context context) {
        SharedPreferences        prefs  = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        // Save the setting
        editor.putInt(AppConstants.PREFERENCE_LAST_LAUNCH_VERSION, getVersionCode(context));
        editor.commit();
    }
}
