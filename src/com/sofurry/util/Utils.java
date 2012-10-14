package com.sofurry.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;


public class Utils {
	private static SharedPreferences pref = null;
	
	public static void initUtils(Context context) {
		pref = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static SharedPreferences getPreferences() {
		return pref;
	}
	
	public static SharedPreferences getPreferences(Context context) {
		if (context != null)
			initUtils(context);

		return pref;
	}

	public static int dp_to_px(Context context, float dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
                dp, context.getResources().getDisplayMetrics());
	}
	
	public static void showYesNoDialog(Context context, String title, String msg, DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

	    builder.setTitle(title);
	    builder.setMessage(msg);

	    if (yes == null) {
	    	builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	            }

	        });
	    } else
	    	builder.setPositiveButton("YES", yes );

	    if (no == null) {
	    	builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	            }
	        });
	
	    }else
	    	builder.setNegativeButton("NO", no);

	    builder.create().show();

	}
}
