package com.sofurry;

import com.sofurry.requests.ProgressSignal;

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
	 * Shows the progress Dialog with progressbar
	 * @param msg
	 */
	public void showAsProgressBar(String msg) {
		ProgressDialog pbarDialog;

		pbarDialog = new ProgressDialog( act );

		//After that, just set the Progress Style to STYLE_HORIZONTAL,

		pbarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		//Also enter the dialog message text to whatever you want and whether it is cancelable or not.

		pbarDialog.setMessage(msg);

		pbarDialog.setCancelable(false);
		
		pbarDialog.show();

		//Read more: http://www.brighthub.com/mobile/google-android/articles/43168.aspx#ixzz10FQ3en3x
	}
	
	/**
	 * Sets the progress to the current progress value
	 * @param prg
	 */
	public void setProgress(ProgressSignal prg) {
		pd.setMax(prg.goal);
		pd.setProgress(prg.progress);
	}
	
	/**
	 * Changes the message currently shown in the progress dialog
	 * @param msg
	 */
	public void setMessage(String msg) {
		pd.setMessage(msg);
	}
	
	/**
	 * Hides the progress Dialog
	 */
	public void hideProgressDialog() {
		if (pd != null && pd.isShowing())
			  pd.dismiss();
	}


}
