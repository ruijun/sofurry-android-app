package com.sofurry.services;

//~--- imports ----------------------------------------------------------------

import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 */
public class PmNotificationService
        extends WakefulIntentService {
    /**
     * Constructs ...
     *
     */
    public PmNotificationService() {
        super("PmNotificationService");
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     *
     * @param intent
     */
    @Override
    protected void doWakefulWork(Intent intent) {
        // TODO Auto-generated method stub
    }
}
