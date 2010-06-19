package com.sofurry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends Activity {

	Button buttonPMs;
	Button buttonChat;
	Button buttonLogbook;
	Button buttonStories;
	Button buttonArt;
	Button buttonMusic;
	Button buttonJournals;
	Button buttonSettings;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Authentication.loadAuthenticationInformation(this);
		setContentView(R.layout.mainmenu);
		buttonPMs = (Button) findViewById(R.id.pms);
		buttonChat = (Button) findViewById(R.id.chat);
		buttonLogbook = (Button) findViewById(R.id.logbook);
		buttonStories = (Button) findViewById(R.id.stories);
		buttonArt = (Button) findViewById(R.id.art);
		buttonMusic = (Button) findViewById(R.id.music);
		buttonJournals = (Button) findViewById(R.id.journals);
		buttonSettings = (Button) findViewById(R.id.settings);

		buttonStories.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				launchStoriesList();
			}
		});

		buttonPMs.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				launchPMList();
			}
		});

		buttonChat.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				launchChat();
			}
		});

		buttonSettings.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				launchAccountActivity();
			}
		});

	}

	private void launchStoriesList() {
		Intent intent = new Intent(this, ListStories.class);
		startActivityForResult(intent, AppConstants.ACTIVITY_STORIESLIST);
	}

	private void launchPMList() {
		Intent intent = new Intent(this, ListPM.class);
		startActivityForResult(intent, AppConstants.ACTIVITY_PMLIST);
	}

	private void launchAccountActivity() {
		Intent intent = new Intent(this, AccountActivity.class);
		startActivity(intent);
	}

	private void launchChat() {
		Intent intent = new Intent(this, ChatActivity.class);
		startActivity(intent);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null)
		{
			Bundle extras = intent.getExtras();
			if (extras != null) {
			// 	General error handling
				String errorMessage = extras.getString("errorMessage");
				if (errorMessage != null) {
					new AlertDialog.Builder(MainMenu.this).setMessage(errorMessage).show();
				}
				
				switch (requestCode) {
				case AppConstants.ACTIVITY_STORIESLIST:
					break;
				case AppConstants.ACTIVITY_PMLIST:
					break;
				}
			}
		}
	}

}
