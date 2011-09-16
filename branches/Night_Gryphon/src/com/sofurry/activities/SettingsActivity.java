package com.sofurry.activities;

//~--- imports ----------------------------------------------------------------

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.util.BootVersionChecker;


//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 */
public class SettingsActivity
        extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {
    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);
    }

    /**
     * Method description
     *
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Unregister listener
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Method description
     *
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Register listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Method description
     *
     *
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ((key.equals(AppConstants.PREFERENCE_PM_CHECK_INTERVAL))
                || (key.equals(AppConstants.PREFERENCE_PM_ENABLE_CHECKS))) {
            // Schedule, reschedule or cancel the alarm
            BootVersionChecker.scheduleAlarm(getApplicationContext());
        }
    }
}
