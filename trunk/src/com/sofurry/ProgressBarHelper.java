package com.sofurry;

import android.app.Activity;
import android.app.ProgressDialog;

/**
 * @author Rangarig
 *
 * Helper class for progressbar stuff
 */
public class ProgressBarHelper {
	
	private ProgressDialog pd;  // The progress dialog to use
	private Activity act;		// Connection to the activity this helper is for
		
	public ProgressBarHelper(Activity act) {
		this.act = act;
	}
	
	/**
	 * Shows the progress Dialog
	 * @param msg
	 */
	public void showProgressDialog(String msg) {
		pd = ProgressDialog.show(act, msg, "Please wait", true, false);
	}
	
	/**
	 * Hides the progress Dialog
	 */
	public void hideProgressDialog() {
		if (pd != null && pd.isShowing())
			  pd.dismiss();
	}


}
