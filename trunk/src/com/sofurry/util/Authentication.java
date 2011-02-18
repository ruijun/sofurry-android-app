package com.sofurry.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.sofurry.AppConstants;

public class Authentication {

	private static String authenticationPadding = "@6F393fk6FzVz9aM63CfpsWE0J1Z7flEl9662X";
	private static long currentAuthenticationSequence = 0;
	private static String username = null;
	private static String password = null;
	private static String salt = "";
	
	public static void loadAuthenticationInformation(final Activity activity) {
	    SharedPreferences credentials = activity.getSharedPreferences(AppConstants.PREFS_NAME, 0);
	    username = credentials.getString("username", "");
	    password = credentials.getString("password", "");
	    salt = credentials.getString("salt", "");
	}

	public static void savePreferences(final Activity activity) {
	      SharedPreferences settings = activity.getSharedPreferences(AppConstants.PREFS_NAME, 0);
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putString("username", username);
	      editor.putString("password", password);
	      editor.putString("salt", salt);
	      editor.commit();
	}

	//Get the MD5 sum of a given input string
	private static String getMd5Hash(final String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);
			while (md5.length() < 32)
				md5 = "0" + md5;
			return md5;
		} catch (NoSuchAlgorithmException e) {
			Log.e(AppConstants.TAG_STRING, "MD5: " + e.getMessage());
			return null;
		}
	}
	
	//Create a has using the current authentication sequence counter, thus "salting" the hash. 
	public static String generateRequestHash() {
		String hashedPassword = getMd5Hash(password + salt);
	    String hash = getMd5Hash(hashedPassword + authenticationPadding + currentAuthenticationSequence);
	    Log.d(AppConstants.TAG_STRING, "Auth: Password: " + hashedPassword +
	    			" padding: " + authenticationPadding + 
	    			" sequence: " + currentAuthenticationSequence +
	    			" salt: " + salt);
	    return hash;
	}

	/**
	 * Returns true, if Authentification Parameters are available
	 * @return
	 */
	public static boolean useAuthentication() {
		return (getUsername() != null && getUsername().trim().length() > 0);
	}
	
	/**
	 * Adds Authentication Parameters to a Request, if Authentication Credentials are available
	 * @param queryParams
	 * The Parameters to add Authentification to
	 */
	public static void addAuthParametersToQuery(Map<String, String> queryParams) {
		if (!useAuthentication()) return;
		//Map<String, String> result = new HashMap<String, String>(queryParams);
		queryParams.put("otpuser", username);
		queryParams.put("otphash", generateRequestHash());
		queryParams.put("otpsequence", ""+currentAuthenticationSequence);
		currentAuthenticationSequence = currentAuthenticationSequence+1;
		//return result;
	}
		
	public static long getCurrentAuthenticationSequence() {
		return currentAuthenticationSequence;
	}

	public static void setCurrentAuthenticationSequence(long newSequence) {
		currentAuthenticationSequence = newSequence;
	}

	public static void setCurrentAuthenticationPadding(String newPadding) {
		authenticationPadding = newPadding;
	}

	public static void setCurrentAuthenticationSalt(String newSalt) {
		salt = newSalt;
	}

	public static String getUsername() {
		return username;
	}

	public static String getPassword() {
		return password;
	}
	
	public static void updateAuthenticationInformation(Activity activity, String newUsername, String newPassword) {
		username = newUsername;
		password = newPassword;
		savePreferences(activity);
	}
	
	/**
	 * Check if passed json string contains data indicating a sequence mismatch, as well as the new sequence data
	 * @param httpResult
	 * @return true if no sequence data found or sequence correct, false if the request needs to be resent with the new enclosed sequence data
	 * @throws JSONException 
	 */
	public static boolean parseResponse(String httpResult) {
		try {
			//check for OTP sequence json and parse it.
			Log.d(AppConstants.TAG_STRING, "Auth.parseResponse: "+httpResult);
			JSONObject jsonParser;
			jsonParser = new JSONObject(httpResult);
			int messageType = jsonParser.getInt("messageType");
			if (messageType == AppConstants.AJAXTYPE_OTPAUTH) {
				int newSequence = jsonParser.getInt("newSequence");
				String newPadding = jsonParser.getString("newPadding");
				String newSalt = jsonParser.getString("salt");
				String otpVersion = jsonParser.getString("version");
				Log.d(AppConstants.TAG_STRING, "Auth.parseResponse: OTP Version: " + otpVersion + 
							" new Sequence: " + newSequence +
							" new Padding: " + newPadding +
							" new salt: " + newSalt );
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

}
