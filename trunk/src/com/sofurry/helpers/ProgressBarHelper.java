package com.sofurry.helpers;

import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import com.sofurry.Quotes;
import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.requests.ProgressSignal;

/**
 * @author Rangarig
 *
 * Helper class for progressbar stuff
 */
public class ProgressBarHelper {
	
	private ProgressDialog pd;  // The progress dialog to use
	private Activity act;		// Connection to the activity this helper is for
	private ICanCancel cancelReceiver = null; // The object to be called if the progress bar is canceled
	private Random random = new Random();
	
	/**
	 * Creates a progressbar helper
	 * @param act
	 * The activity to use as context
	 * @param cancelReceiver
	 * The class to call when a cancel request is made
	 */
	public ProgressBarHelper(Activity act, ICanCancel cancelReceiver) {
		this.act = act;
		this.cancelReceiver = cancelReceiver;
	}
	
	/**
	 * Shows the progress Dialog
	 * @param msg
	 */
	public void showProgressDialog(String msg) {
		String quote = Quotes.quotes[random.nextInt(Quotes.quotes.length)];
		pd = ProgressDialog.show(act, msg, quote, true, true);
		setCancelListener();
	}
	
	/**
	 * Sets the listener that is called when the Progress dialog is canceled
	 */
	private void setCancelListener() {
		pd.setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				if (cancelReceiver != null)
					cancelReceiver.cancel();
			}
		});
	}
	
	/**
	 * Shows the progress Dialog with progressbar
	 * @param msg
	 */
	public void showAsProgressBar(String msg) {
		pd = new ProgressDialog( act );

		//After that, just set the Progress Style to STYLE_HORIZONTAL,
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		//Also enter the dialog message text to whatever you want and whether it is cancelable or not.
		pd.setMessage(msg);

		pd.setCancelable(true);
		setCancelListener();

		// Will allow the application to react to cancel reqeusts
		
		pd.show();

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
