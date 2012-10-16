package com.sofurry.activities;

//~--- imports ----------------------------------------------------------------

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.os.AsyncTask;
import android.os.Bundle;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.mobileapi.core.AuthenticationHandler;
import com.sofurry.storage.FileStorage;
import com.sofurry.storage.ImageStorage;
import com.sofurry.util.BootVersionChecker;
import com.sofurry.util.Utils;


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

        // validate thumb cleanup period value
        getPreferenceScreen().findPreference(AppConstants.PREFERENCE_THUMB_CLEAN_PERIOD).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					Integer.parseInt(newValue.toString());
					return true;
				}catch(NumberFormatException nfe) {
					Toast.makeText(SettingsActivity.this, "ERROR: '"+newValue+"' is an invalid number", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});
        
        // request reload auth on credentials change
/*        getPreferenceScreen().findPreference(AppConstants.PREFERENCE_USERNAME).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					AuthenticationHandler.triggerReloadAuth(SettingsActivity.this);
					return true;
				}catch(Exception e) {
					Toast.makeText(SettingsActivity.this, "ERROR: cant reload credentials", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});

        getPreferenceScreen().findPreference(AppConstants.PREFERENCE_PASSWORD).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					AuthenticationHandler.triggerReloadAuth(SettingsActivity.this);
					return true;
				}catch(Exception e) {
					Toast.makeText(SettingsActivity.this, "ERROR: cant reload credentials", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});/**/
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ((key.equals(AppConstants.PREFERENCE_PM_CHECK_INTERVAL))
                || (key.equals(AppConstants.PREFERENCE_PM_ENABLE_CHECKS))) {
            // Schedule, reschedule or cancel the alarm
            BootVersionChecker.scheduleAlarm(getApplicationContext());
        } else if (	(key.equals(AppConstants.PREFERENCE_USERNAME)) || 
        			(key.equals(AppConstants.PREFERENCE_PASSWORD))) {
        	try {
				AuthenticationHandler.loadAuthenticationInformation(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
        } else if (key.equals(AppConstants.PREFERENCE_USE_CUSTOM_THUMBS)) {
        	Utils.showYesNoDialog(this, "Clear thumb cache", 
        						"To reload already cached thumbnails you should clear thumbnails cache. Clear cache?", 
        						new DialogInterface.OnClickListener() { // yes
									public void onClick(DialogInterface dialog, int which) {
										// start cleanup in async thread
										(new AsyncTask<Integer, Integer, Integer>() {
											@Override
											protected Integer doInBackground(Integer... params) {
												try {
													FileStorage.cleanup(FileStorage.getPath(ImageStorage.THUMB_PATH));
												} catch (Exception e) {
													e.printStackTrace();
												}
												return null;
											}
										}).execute();
									}
								}, 
        						null // no
        						);
        }
    }
}
