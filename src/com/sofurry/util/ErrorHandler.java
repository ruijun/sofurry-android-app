package com.sofurry.util;

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
	public static void showError(Activity act, Exception e) {
		Log.d("Exception", e.getMessage());
    	Toast.makeText(act.getApplicationContext(), "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
	}

}
