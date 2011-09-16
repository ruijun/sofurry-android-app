package com.sofurry.receivers;

//~--- imports ----------------------------------------------------------------

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.sofurry.AppConstants;
import com.sofurry.services.PmNotificationService;


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
    	Log.i(AppConstants.TAG_STRING, "Triggering alarm");
    	
        WakefulIntentService.sendWakefulWork(context,
                                             PmNotificationService.class);
    }
}
