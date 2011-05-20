package com.sofurry.services;

//~--- imports ----------------------------------------------------------------

import com.sofurry.AppConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 */
public class BootVersionChecker {
    public static final String LAST_LAUNCH_VERSION_KEY = "lastLaunchVersion";


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
        SharedPreferences prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);

        // Check if the preference value matches the version code
        if (prefs.getInt(LAST_LAUNCH_VERSION_KEY, 0) != getVersionCode(context)) {
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
        SharedPreferences        prefs  = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save the setting
        editor.putInt(LAST_LAUNCH_VERSION_KEY, getVersionCode(context));
        editor.commit();
    }
}
