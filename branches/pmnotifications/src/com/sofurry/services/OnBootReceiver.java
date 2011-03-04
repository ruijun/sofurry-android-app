package com.sofurry.services;

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
    private static final int PERIOD = 300000;    // 5 Minutes


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
        AlarmManager  manager       = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent        alarmIntent   = new Intent(context, OnAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                             SystemClock.elapsedRealtime() + 60000,
                             PERIOD,
                             pendingIntent);
    }
}
