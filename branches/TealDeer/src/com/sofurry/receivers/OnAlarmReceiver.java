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

public class OnAlarmReceiver
        extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(AppConstants.TAG_STRING, "Triggering alarm");
    	
        WakefulIntentService.sendWakefulWork(context,
                                             PmNotificationService.class);
    }
}
