package com.sofurry.services;

//~--- imports ----------------------------------------------------------------

import com.sofurry.AppConstants;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.SystemClock;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 */
public class OnBootReceiver
        extends BroadcastReceiver {


    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Context            newContext    = context.getApplicationContext();
        AlarmManager       manager       = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent             alarmIntent   = new Intent(newContext, OnAlarmReceiver.class);
        PendingIntent      pendingIntent = PendingIntent.getBroadcast(newContext, 0, alarmIntent, 0);
        BootVersionChecker bvc           = new BootVersionChecker();

        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                             SystemClock.elapsedRealtime() + AppConstants.ALARM_CHECK_DELAY_FIRST,
                             AppConstants.ALARM_CHECK_DELAY_PERIOD,
                             pendingIntent);

        // Tell the system that we have set an alarm
        bvc.setHasLaunched(newContext);
    }
}
