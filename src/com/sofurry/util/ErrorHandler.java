package com.sofurry.util;

import com.sofurry.AppConstants;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Rangarig
 *
 * A class that provides handy methods for handling errors.
 * 
 * Ok, so far only one
 */
public class ErrorHandler {
	public static void justLogError(Exception e) {
		if (e.getClass().equals(NullPointerException.class)) e= new Exception("NullPointerException");
		Log.d(AppConstants.TAG_STRING, "Exception: " + e.getMessage());
	}

	
	public static void showError(Activity act, Exception e) {
		justLogError(e);
    	Toast.makeText(act.getApplicationContext(), "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
	}

}
