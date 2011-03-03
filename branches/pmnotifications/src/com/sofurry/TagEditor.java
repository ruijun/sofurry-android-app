package com.sofurry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TagEditor extends Activity {
	
	private Button buttonOk;
	private EditText textfieldTags;
	private Button buttonGender;
	private Button buttonSpecies;
	private Button buttonFetish;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tageditorlayout);
	    buttonOk = (Button) findViewById(R.id.TagOkButton);
	    buttonGender = (Button) findViewById(R.id.TagGenderButton);
	    buttonSpecies = (Button) findViewById(R.id.TagSpeciesButton);
	    buttonFetish = (Button) findViewById(R.id.TagFetishButton);
	    textfieldTags = (EditText) findViewById(R.id.Tags);
	    buttonOk.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				Log.i(AppConstants.TAG_STRING, "Logon: ok clicked");
				closeActivity();
			}});
	    buttonGender.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				showGenderDialog();
			}});
	    buttonSpecies.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				showSpeciesDialog();
			}});
	    buttonFetish.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				showFetishDialog();
			}});
	    
	}
	
	private String[] tagList = null;
	
	/**
	 * Shows a dialog that offers some gender choices
	 */
	private void showGenderDialog() {
		chooseTag(new String[] {"M/M", "M/F", "F/F"});
	}
	
	/**
	 * Shows a dialog that offers some species choices
	 */
	private void showSpeciesDialog() {
		chooseTag(new String[] {"Dragon", "Horse", "Orca", "Fox", "Wulf", "Elephant"});
	}

	/**
	 * Shows a dialog that offers some fetish choices
	 */
	private void showFetishDialog() {
		chooseTag(new String[] {"Anal" , "Oral", "Inflation", "Rape", "Oral"});
	}
	
	/**
	 * Shows the actual Dialog, and redirects choices to the add Tag method
	 * @param myTags
	 */
	private void chooseTag(String[] myTags) {
		tagList = myTags;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose a Tag");
		builder.setItems(tagList, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	addToTagText(tagList[item]);
		    }
		});
		AlertDialog roomchooser = builder.create();
		roomchooser.show();
	}
	
	/**
	 * Adds a tag to the list of tags already in place
	 * @param tag
	 * The tag to be added
	 */
	private void addToTagText(String tag) {
		if (textfieldTags.getText().length() > 0) {
			if (!textfieldTags.getText().toString().endsWith(" ")) textfieldTags.getText().append(" ");
		}
		textfieldTags.getText().append(tag);
	}
	
	/**
	 * Closes this activity
	 */
	private void closeActivity() {
		Log.i(AppConstants.TAG_STRING,"closeActivity: Closing...");
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		mIntent.putExtra("tags", textfieldTags.getText().toString());
		setResult(RESULT_OK, mIntent);
		finish();
	}


}
