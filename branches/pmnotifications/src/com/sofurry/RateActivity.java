package com.sofurry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

public class RateActivity extends Activity {
	
	private Button buttonOk;
	private RatingBar rb = null;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.starsrater);
	    buttonOk = (Button) findViewById(R.id.RateOkButton);
	    rb = (RatingBar) findViewById(R.id.ArtRatingBar);
	    buttonOk.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				closeActivity();
			}});
	    
	}
	
	/**
	 * Closes this activity
	 */
	private void closeActivity() {
		Log.i(AppConstants.TAG_STRING,"closeActivity: Closing...");
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		mIntent.putExtra("rating", (int)rb.getRating());
		setResult(RESULT_OK, mIntent);
		finish();
	}


}
