package com.sofurry.services;

//~--- imports ----------------------------------------------------------------

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 */
public class OnAlarmReceiver
        extends BroadcastReceiver {
    /**
     * Method description
     *
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        WakefulIntentService.sendWakefulWork(context,
                                             PmNotificationService.class);
    }
}
