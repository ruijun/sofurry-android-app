package com.sofurry.receivers;

//~--- imports ----------------------------------------------------------------

import com.sofurry.util.BootVersionChecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 */
public class OnBootReceiver
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
        Context newContext = context.getApplicationContext();

        BootVersionChecker.scheduleAlarm(newContext);
    }
}
