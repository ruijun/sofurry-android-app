package com.sofurry.services;

//~--- imports ----------------------------------------------------------------

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
    public static final int PERIOD = 20000;    // 5 Minutes (300000)


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
                             SystemClock.elapsedRealtime() + 10000,
                             PERIOD,
                             pendingIntent);

        // Tell the system that we have set an alarm
        bvc.setHasLaunched(newContext);
    }
}
