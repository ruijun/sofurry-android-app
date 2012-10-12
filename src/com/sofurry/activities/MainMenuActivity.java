package com.sofurry.activities;


import java.util.Date;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.base.classes.ActivityWithRequests;
import com.sofurry.base.classes.SFBrowseActivity;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.core.AuthenticationHandler;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.mobileapi.downloaders.ContentDownloader;
import com.sofurry.requests.AndroidRequestWrapper;
import com.sofurry.requests.DataCall;
import com.sofurry.storage.FileStorage;
import com.sofurry.storage.ImageStorage;
import com.sofurry.util.BootVersionChecker;
import com.sofurry.util.Utils;


/**
 * Main menu activity
 *
 * @author SoFurry
 */
public class MainMenuActivity
        extends ActivityWithRequests {
    private Button  buttonChat_;
    private Button  buttonForums_;
    private Button  buttonLogbook_;
    private Button  buttonPMs_;
    private boolean mustReloadAuthInfo_ = true;
    private int     messageCount_       = -1;
    private long    lastCheck_          = -1;
    
    private AsyncTask<Integer, Integer, Integer> cleaner = null;

    /**
     * Method description
     *
     *
     * @param v
     */
    public void buttonClick(View v) {
        int    activityId = -1;
        int    id         = v.getId();
        Intent intent     = null;

        switch (id) {
            case R.id.pms:
                intent     = new Intent(this, ListPMActivity.class);
                activityId = AppConstants.ACTIVITY_PMLIST;

                break;

            case R.id.chat:
                intent = new Intent(this, ChatActivity.class);

                break;

            case R.id.logbook:
                break;

            case R.id.stories:
                intent     = new Intent(this, SFBrowseActivity.class);
                intent.putExtra("ContentType", ContentType.stories);
                activityId = AppConstants.ACTIVITY_STORIESLIST;

                break;

            case R.id.art:
                intent     = new Intent(this, SFBrowseActivity.class);
                intent.putExtra("ContentType", ContentType.art);
                activityId = AppConstants.ACTIVITY_GALLERYART;

                break;

            case R.id.music:
                intent     = new Intent(this, SFBrowseActivity.class);
                intent.putExtra("ContentType", ContentType.music);
                activityId = AppConstants.ACTIVITY_MUSICLIST;

                break;

            case R.id.journals:
                intent     = new Intent(this, SFBrowseActivity.class);
                intent.putExtra("ContentType", ContentType.journals);
                activityId = AppConstants.ACTIVITY_JOURNALSLIST;

                break;

            case R.id.settings:
                intent              = new Intent(this, SettingsActivity.class);
                activityId          = AppConstants.ACTIVITY_SETTINGS;
                mustReloadAuthInfo_ = true;

                break;

            case R.id.forums:
                break;
        }

        // Check if we need to launch an activity
        if (intent != null) {
            // Will we be wanting the result?
            if (activityId > -1) {
                // Indeed we will
                startActivityForResult(intent, activityId);
            } else {
                // We don't care about the result, no
                startActivity(intent);
            }
        }
    }

    private void checkButtonDisabledState() {
        if ((AuthenticationHandler.getUsername() == null) || (AuthenticationHandler.getUsername().trim().length() <= 0)
                || (AuthenticationHandler.getPassword() == null) || (AuthenticationHandler.getPassword().trim().length() <= 0)) {
            buttonPMs_.setEnabled(false);
            buttonChat_.setEnabled(false);
        } else {
            buttonPMs_.setEnabled(true);
            buttonChat_.setEnabled(true);
        }
    }

    private void checkPmCount() {
        /*
         *  Limit how often it'll refresh itself, and only do so if information
         * has been filled out
         */
        if ((buttonPMs_.isEnabled()) && (new Date().getTime() > lastCheck_ + 300000)) {
//            pbh.showProgressDialog("Fetching data...");
            
    		Request req = ApiFactory.createUnreadPMCount();
    		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
    		arw.exec(new DataCall() { public void call() { handlePMCount((JSONObject)arg1);	} });


            lastCheck_ = new Date().getTime();
        }
    }

    // user profile support
    private void checkProfile() {
    		Request req = ApiFactory.createGetUserProfile();
    		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
    		arw.exec(new DataCall() { public void call() { handleProfileRequest((JSONObject)arg1);	} });
    }

    /**
     * Handles the feedback from the UnreadPMCountRequest
     * @param obj
     * @throws Exception
     */
    public void handleProfileRequest(JSONObject obj) {
    	try {
    		ApiFactory.myUserProfile.LoadFromJSON(obj);
    		updateProfile();
    		
		} catch (Exception e) {
			onError(e);
		}
    }

    public void updateProfile() {
    	if (ApiFactory.myUserProfile.userID <=0) {
    		return;
    	}
    	
    	try {
    		TextView nickname = (TextView) findViewById(R.id.user_profile_nickname);
    		if (nickname != null) {
    			nickname.setVisibility(View.VISIBLE);
    			nickname.setText(ApiFactory.myUserProfile.username);
    		}

    		Bitmap bmp = ApiFactory.myUserProfile.getAvatar();
    		
/*    		Bitmap bmp = ImageStorage.loadUserIcon(user_id);
    		
    		if (bmp == null) {
    			ContentDownloader.downloadFile(ApiFactory.getUserIconURL(user_id), ImageStorage.getUserIconPath(user_id), null);

    			bmp = ImageStorage.loadUserIcon(user_id);
    		} */
    		
    		if (bmp != null) {
    			ImageView image = (ImageView) findViewById(R.id.user_profile_avatar);
    			if (image != null) {
    				image.setVisibility(View.VISIBLE);
    				image.setImageBitmap(bmp);
    			}
    		}
		} catch (Exception e) {
			onError(e);
		}
    }
    
    /**
     * Method description
     *
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check button state
        checkButtonDisabledState();

        if (intent != null) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                // General error handling
                String errorMessage = extras.getString("errorMessage");

                if (errorMessage != null) {
                    new AlertDialog.Builder(MainMenuActivity.this).setMessage(errorMessage).show();
                }

                switch (requestCode) {
                    case AppConstants.ACTIVITY_STORIESLIST:
                        break;

                    case AppConstants.ACTIVITY_PMLIST:
                        break;
                }
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve authentication info
        AuthenticationHandler.loadAuthenticationInformation(this);

        mustReloadAuthInfo_ = false;

        // If the notification alarm service hasn't been scheduled, do so
        setupAlarmIfNeeded();

        // Set the content view
//        setContentView(R.layout.mainmenu);
        setContentView(R.layout.mainmenu_with_profile);

        // Find the buttons we'll be using
        buttonPMs_     = (Button) findViewById(R.id.pms);
        buttonChat_    = (Button) findViewById(R.id.chat);
        buttonLogbook_ = (Button) findViewById(R.id.logbook);
        buttonForums_  = (Button) findViewById(R.id.forums);

        // Disable forums and logbook buttons for now
        buttonForums_.setEnabled(false);
        buttonLogbook_.setEnabled(false);

        // Disable buttons as need be
        checkButtonDisabledState();

        // Retrieve information from the instance state object, if need be
        if (savedInstanceState != null) {
            messageCount_ = (Integer) retrieveObject("messageCount");
//            user_id = (Integer) retrieveObject("user_id");
//            user_nickname = (String) retrieveObject("user_nickname");
            cleaner = (AsyncTask<Integer, Integer, Integer>) retrieveObject("cleaner");

            updateButtons();
            updateProfile();
        } else {
            // Fetch the information from the server instead
            checkPmCount();
            checkProfile();
        }
        
        if (cleaner == null) {
        	cleaner = new AsyncTask<Integer, Integer, Integer>() {

				@Override
				protected Integer doInBackground(Integer... params) {
					try {
				        SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
				        int days = Integer.parseInt(prefs.getString(AppConstants.PREFERENCE_THUMB_CLEAN_PERIOD, "3"));
				        if (days >= 0)
				        	FileStorage.cleanold(FileStorage.getPath(ImageStorage.THUMB_PATH)+"/", days);
					} catch (Exception e) {
						e.printStackTrace();
					}
					cleaner = null;
					return null;
				}
			};
			cleaner.execute(null);
        }
    }

    /**
     * Handles the feedback from the UnreadPMCountRequest
     * @param obj
     * @throws Exception
     */
    public void handlePMCount(JSONObject obj) {
    	
    	try {
            // Hide progress bar
            pbh.hideProgressDialog();
            // Fetch the number
            messageCount_ = obj.getInt("unreadpmcount");

            // Update display
            updateButtons();
		} catch (Exception e) {
			onError(e);
		}
    }

    /**
     * Method description
     *
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mustReloadAuthInfo_) {
            AuthenticationHandler.loadAuthenticationInformation(this);
        	checkProfile();
        }

        if (ApiFactory.myUserProfile.userID < 0) 
        	checkProfile();
        
        checkButtonDisabledState();
        checkPmCount();
    }

    /**
     * Method description
     *
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        storeObject("messageCount", (Integer) messageCount_);
//        storeObject("user_id", (Integer) user_id);
//        storeObject("user_nickname", (String) user_nickname);
        storeObject("cleaner", cleaner);
    }

    /**
     * Method description
     *
     */
    protected void setupAlarmIfNeeded() {
        BootVersionChecker.scheduleAlarm(getApplicationContext(), true);
    }

    /**
     * Method description
     *
     */
    protected void updateButtons() {
        // Alter the PM button based on the number of messages
        if (messageCount_ > 0) {
            buttonPMs_.setTypeface(Typeface.create((String) null, Typeface.BOLD));
        } else {
            buttonPMs_.setTypeface(Typeface.create((String) null, Typeface.NORMAL));
        }

        // Including the text
        buttonPMs_.setText("PMs (" + messageCount_ + ")");
    }

//    /**
//     * Method description
//     *
//     *
//     * @return
//     */
//    protected AjaxRequest getCheckParameters() {
//        AjaxRequest req = new AjaxRequest();
//
//        // Set request parameters
//        req.addParameter("f", "unreadpmcount");
//
//        // Set request ID
//        req.setRequestID(AppConstants.REQUEST_ID_FETCHDATA);
//
//        // Return result
//        return req;
//    }
}
