package com.sofurry.mobileapi.core;

import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import android.util.Log;

import com.sofurry.AppConstants;

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
    private static String authenticationPadding         = "@6F393fk6FzVz9aM63CfpsWE0J1Z7flEl9662X";
    private static String password                      = null;
    private static String salt                          = "";
    private static String username                      = null;
    private static long   currentAuthenticationSequence = 0;


    /**
     * Adds Authentication Parameters to a Request, if Authentication Credentials are available
     * @param queryParams
     * The Parameters to add Authentification to
     * @param fakeAuth
     * If this is set to true, a fake username will be transmitted, which is required by some parts of the new API
     */
    public static void addAuthParametersToQuery(Map<String, String> queryParams) {
        if (!useAuthentication()) {
            return;
        }

        // Map<String, String> result = new HashMap<String, String>(queryParams);
        queryParams.put("otpuser", username);
        queryParams.put("otphash", generateRequestHash());
        queryParams.put("otpsequence", "" + currentAuthenticationSequence);
//        queryParams.put("otpuser", "gargelblargh");
//        queryParams.put("otphash", "");
//        queryParams.put("otpsequence", "" + currentAuthenticationSequence);

        currentAuthenticationSequence = currentAuthenticationSequence + 1;
    }

    // Create a hash using the current authentication sequence counter, thus "salting" the hash.

    /**
     * Method description
     *
     *
     * @return
     */
    public static String generateRequestHash() {
        String hashedPassword = getMd5Hash(password + salt);
        String hash           = getMd5Hash(hashedPassword + authenticationPadding + currentAuthenticationSequence);

        Log.d(AppConstants.TAG_STRING,
              "Auth: Password: " + hashedPassword + " padding: " + authenticationPadding + " sequence: "
              + currentAuthenticationSequence + " salt: " + salt);

        return hash;
    }

    /**
     * Method description
     *
     *
     * @param context
     */
    public static void loadAuthenticationInformation(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        username = prefs.getString(AppConstants.PREFERENCE_USERNAME, "");
        password = prefs.getString(AppConstants.PREFERENCE_PASSWORD, "");
        salt     = prefs.getString(AppConstants.PREFERENCE_SALT, "");
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

            jsonParser = new JSONObject(httpResult);

            int messageType = jsonParser.getInt("messageType");

            if (messageType == AppConstants.AJAXTYPE_OTPAUTH) {
                int    newSequence = jsonParser.getInt("newSequence");
                String newPadding  = jsonParser.getString("newPadding");
                String newSalt     = jsonParser.getString("salt");
                String otpVersion  = jsonParser.getString("version");

                Log.d(AppConstants.TAG_STRING,
                      "Auth.parseResponse: OTP Version: " + otpVersion + " new Sequence: " + newSequence
                      + " new Padding: " + newPadding + " new salt: " + newSalt);
                setCurrentAuthenticationSequence(newSequence);
                setCurrentAuthenticationPadding(newPadding);
                setCurrentAuthenticationSalt(newSalt);

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
     */
    public static void savePreferences(final Context context) {
        SharedPreferences        prefs  = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(AppConstants.PREFERENCE_USERNAME, username);
        editor.putString(AppConstants.PREFERENCE_PASSWORD, password);
        editor.putString(AppConstants.PREFERENCE_SALT, salt);
        editor.commit();
    }

    /**
     * Method description
     *
     *
     * @param activity
     * @param newUsername
     * @param newPassword
     */
    public static void updateAuthenticationInformation(Activity activity, String newUsername, String newPassword) {
        username = newUsername;
        password = newPassword;

        savePreferences(activity);
    }

    /**
     * Returns true, if Authentification Parameters are available
     * @return
     */
    public static boolean useAuthentication() {
        return ((getUsername() != null) && (getUsername().trim().length() > 0));
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public static long getCurrentAuthenticationSequence() {
        return currentAuthenticationSequence;
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
    public static String getPassword() {
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

    /**
     * Method description
     *
     *
     * @param newPadding
     */
    public static void setCurrentAuthenticationPadding(String newPadding) {
        authenticationPadding = newPadding;
    }

    /**
     * Method description
     *
     *
     * @param newSalt
     */
    public static void setCurrentAuthenticationSalt(String newSalt) {
        salt = newSalt;
    }

    /**
     * Method description
     *
     *
     * @param newSequence
     */
    public static void setCurrentAuthenticationSequence(long newSequence) {
        currentAuthenticationSequence = newSequence;
    }
}
