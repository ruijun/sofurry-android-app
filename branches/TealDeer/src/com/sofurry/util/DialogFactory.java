package com.sofurry.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogFactory {
	public static AlertDialog createErrorDialog(Context context, String title, String errorMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		// Set title if present
		if ((title != null) && (title.length() > 0)) {
			builder.setTitle(title);
		} else {
			builder.setTitle("Error");
		}
		
		// Then set the message text
		builder.setMessage("An error occurred:\n\n" + errorMessage);
		
		// And finally set the remaining options
		builder.setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
		       });
		
		// Return result
		return builder.create();
	}
}
