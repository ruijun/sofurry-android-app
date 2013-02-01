package com.sofurry.mobileapi.core;

import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import android.util.Log;

import com.sofurry.AppConstants;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.ApiFactory.SFUserProfile;
import com.sofurry.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Map;



/**
 * @author Rangarig
 *
 * The Authentification Handler contains all the Methods used to handle Authentification for the SOFurry api
 */
public class AuthenticationHandler {
	
	
    public static final int AJAXTYPE_OTPAUTH = 6;		// Message reply if there is an authentification failure
	
    private static String authenticationPadding         = "@6F393fk6FzVz9aM63CfpsWE0J1Z7flEl9662X";
    private static String password                      = null;
    private static String salt                          = "";
    private static String username                      = null;
    private static long   currentAuthenticationSequence = 0;
    private static Object authSyncDummy = new Object();
    private static Boolean mustReloadAuth = true;


    /**
     * Adds Authentication Parameters to a Request, if Authentication Credentials are available
     * @param queryParams
     * The Parameters to add Authentification to
     * @param fakeAuth
     * If this is set to true, a fake username will be transmitted, which is required by some parts of the new API
     */
    public static void addAuthParametersToQuery(Map<String, String> queryParams) {
        try {
			if (!useAuthentication()) // this reload auth info if required
			    return;
		} catch (Exception e) {
			e.printStackTrace();
			return; // the only reason for this is
		}

        synchronized (authSyncDummy) {
            queryParams.put("otpuser", username);
            queryParams.put("otphash", generateRequestHash());
            queryParams.put("otpsequence", "" + currentAuthenticationSequence);

            currentAuthenticationSequence += 1;
		}
    }

    /**
     * Create a hash using the current authentication sequence counter, thus "salting" the hash.
     *
     * @return
     */
    private static String generateRequestHash() {
        String hashedPassword = getMd5Hash(password + salt);
        String hash           = getMd5Hash(hashedPassword + authenticationPadding + currentAuthenticationSequence);

        Log.d(AppConstants.TAG_STRING,
              "Auth: Password: " + hashedPassword + " padding: " + authenticationPadding + " sequence: "
              + currentAuthenticationSequence + " salt: " + salt);

        return hash;
    }

    /**
     * Set flag to reload auth info next time 
     * @param context - context to load SharedPrefs from
     * @throws Exception
     */
    public static void triggerReloadAuth(Context context) throws Exception {
    	if (Utils.getPreferences() == null)
    		if (context != null)
    			Utils.initUtils(context);
    		else
    			throw new Exception("Must provide context or init Utils to reload auth info");
    	
        mustReloadAuth = true;
        ApiFactory.myUserProfile = new SFUserProfile(); // clean user profile
    }
    
    /**
     * Load auth info from preferences
     *
     * @param context - context to load SharedPrefs from. Can be null if previous request was provide info to cache PrefManager
     * @throws Exception 
     */
    public static void loadAuthenticationInformation(final Context context) throws Exception {
    	if (Utils.getPreferences() == null)
    		if (context != null)
    			Utils.initUtils(context);
    		else
    			throw new Exception("Must provide context or init Utils to load auth info");
    	
    	SharedPreferences prefs = Utils.getPreferences();

        synchronized (authSyncDummy) {
        	String newusername = prefs.getString(AppConstants.PREFERENCE_USERNAME, "");
        	String newpassword = prefs.getString(AppConstants.PREFERENCE_PASSWORD, "");
        	
        	// clean profile if credentials changed
        	if ((! newusername.equals(username)) || (! newpassword.equals(password)))
        		ApiFactory.myUserProfile = new SFUserProfile();
        	
        	username = newusername;
        	password = newpassword;
            salt     = prefs.getString(AppConstants.PREFERENCE_SALT, "");
            mustReloadAuth = false;
		}
    }

    /**
     * Check if passed json string contains data indicating a sequence mismatch, as well as the new sequence data
     * @param httpResult
     * @return true if no sequence data found or sequence correct,
     *             false if the request needs to be resent with the new enclosed sequence data
     */
    public static boolean parseResponse(String httpResult) {
        try {
            // check for OTP sequence json and parse it.
            Log.d(AppConstants.TAG_STRING, "Auth.parseResponse: " + httpResult);

            JSONObject jsonParser;
            try {
                jsonParser = new JSONObject(httpResult);
			} catch (JSONException j) {
				return true; // If the reply is not even a json structure, it cannot be an auth resync request
			}
                
            int messageType = jsonParser.getInt("messageType");

            if (messageType == AJAXTYPE_OTPAUTH) {
                int    newSequence = jsonParser.getInt("newSequence");
                String newPadding  = jsonParser.getString("newPadding");
                String newSalt     = jsonParser.getString("salt");
                String otpVersion  = jsonParser.getString("version");

                
                Log.d(AppConstants.TAG_STRING,
                      "Auth.parseResponse: OTP Version: " + otpVersion + " new Sequence: " + newSequence
                      + " new Padding: " + newPadding + " new salt: " + newSalt);

                synchronized (authSyncDummy) {
                    authenticationPadding = newPadding;
                    salt = newSalt;
                    currentAuthenticationSequence = newSequence;
				}

                return false;
            }
        } catch (JSONException e) {
            Log.d(AppConstants.TAG_STRING, "Auth.parseResponse " + e.toString());
        }

        return true;
    }

    /**
     * Method description
     *
     *
     * @param context
     * @throws Exception 
     */
    public static void savePreferences(final Context context) throws Exception {
    	if (mustReloadAuth) {
    		// we did not load latest auth preferences so should not save non actual info
			try {
				loadAuthenticationInformation(context);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	} else {
        	if (Utils.getPreferences() == null)
        		if (context != null)
        			Utils.initUtils(context);
        		else
        			throw new Exception("Must provide context or init Utils to load auth info");
        	
        	SharedPreferences prefs = Utils.getPreferences();
            SharedPreferences.Editor editor = prefs.edit();

            synchronized (authSyncDummy) {
                editor.putString(AppConstants.PREFERENCE_USERNAME, username);
                editor.putString(AppConstants.PREFERENCE_PASSWORD, password);
                editor.putString(AppConstants.PREFERENCE_SALT, salt);
    		}
            editor.commit();
		}
    }

//    /**
//     * Method description
//     *
//     *
//     * @param activity
//     * @param newUsername
//     * @param newPassword
//     */
//    public static void updateAuthenticationInformation(Activity activity, String newUsername, String newPassword) {
//        username = newUsername;
//        password = newPassword;
//
//        savePreferences(activity);
//    }

    /**
     * Returns true, if Authentification Parameters are available
     * @return
     * @throws Exception 
     */
    public static boolean useAuthentication(Context context) throws Exception {
    	if (mustReloadAuth)
    		loadAuthenticationInformation(context);
    	
        return ((username != null) && (username.trim().length() > 0));
    }

    public static boolean useAuthentication() throws Exception {
    	return useAuthentication(null);
    }
    
    // Get the MD5 sum of a given input string
    private static String getMd5Hash(final String input) {
        try {
            MessageDigest md            = MessageDigest.getInstance("MD5");
            byte[]        messageDigest = md.digest(input.getBytes());
            BigInteger    number        = new BigInteger(1, messageDigest);
            String        md5           = number.toString(16);

            while (md5.length() < 32) {
                md5 = "0" + md5;
            }

            return md5;
        } catch (NoSuchAlgorithmException e) {
            Log.e(AppConstants.TAG_STRING, "MD5: " + e.getMessage());

            return null;
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
/*    public static String getPassword() {
        return password;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public static String getUsername() {
        return username;
    }

//    /**
//     * Method description
//     *
//     *
//     * @param newPadding
//     */
//    public static void setCurrentAuthenticationPadding(String newPadding) {
//        authenticationPadding = newPadding;
//    }
//
//    /**
//     * Method description
//     *
//     *
//     * @param newSalt
//     */
//    public static void setCurrentAuthenticationSalt(String newSalt) {
//        salt = newSalt;
//    }
//
//    /**
//     * Method description
//     *
//     *
//     * @param newSequence
//     */
//    public static void setCurrentAuthenticationSequence(long newSequence) {
//        currentAuthenticationSequence = newSequence;
//    }
}
