package com.sofurry.util;

import com.sofurry.adapters.SFBrowseCache;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.TypedValue;


public class Utils {
	private static SharedPreferences pref = null;
	private static ConnectivityManager ConnectMan = null;
	private static ClipboardManager ClipMan = null;
	private static SFBrowseCache fBrowseCache = null;
	
	public static void initUtils(Context context) {
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		ConnectMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		ClipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		
		if (fBrowseCache == null)
			fBrowseCache = new SFBrowseCache(context);
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

	public static boolean isNumber(Object newValue) {
		try {
			Integer.parseInt(newValue.toString());
			return true;
		}catch(NumberFormatException nfe) {
			return false;
		}
    }

	public static boolean isOnline() {
		return isOnline(null);
	}
	
	public static boolean isOnline(Context context) {
		if (context != null)
			initUtils(context);
		
	    NetworkInfo netInfo = ConnectMan.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}

	public static void setClipboardText(String txt) {
		setClipboardText(null, txt);
	}
	
	public static void setClipboardText(Context context, String txt) {
		if (context != null)
			initUtils(context);

		if (ClipMan != null)
			ClipMan.setText(txt);
	}
	
	public static SFBrowseCache BrowseCache() {
		return BrowseCache(null);
	}
	
	public static SFBrowseCache BrowseCache(Context context) {
		if (context != null)
			initUtils(context);

		return fBrowseCache;
	}
}
