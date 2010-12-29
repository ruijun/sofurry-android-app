package com.sofurry.mainmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sofurry.AccountActivity;
import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.chat.ChatActivity;
import com.sofurry.gallery.GalleryArt;
import com.sofurry.list.ListJournals;
import com.sofurry.list.ListMusic;
import com.sofurry.list.ListPM;
import com.sofurry.list.ListStories;
import com.sofurry.util.Authentication;

/**
 * @author SoFurry
 *
 * Main menu activity
 */
public class MainMenu extends Activity {

	Button buttonPMs;
	Button buttonChat;
	Button buttonLogbook;
	Button buttonStories;
	Button buttonArt;
	Button buttonMusic;
	Button buttonJournals;
	Button buttonSettings;
	Button buttonForums;

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
		buttonForums = (Button) findViewById(R.id.forums);
		
		buttonForums.setEnabled(false);
		buttonLogbook.setEnabled(false);
		//buttonMusic.setEnabled(false);

		checkButtonDisabledState();
		
		buttonStories.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				launchStoriesList();
			}
		});

		buttonArt.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				launchArtGallery();
			}
		});

		buttonMusic.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				launchMusicList();
			}
		});

		buttonJournals.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				launchJournalsList();
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

	private void launchJournalsList() {
		Intent intent = new Intent(this, ListJournals.class);
		startActivityForResult(intent, AppConstants.ACTIVITY_JOURNALSLIST);
	}

	private void launchArtGallery() {
		Intent intent = new Intent(this, GalleryArt.class);
		startActivityForResult(intent, AppConstants.ACTIVITY_GALLERYART);
	}

	private void launchStoriesList() {
		Intent intent = new Intent(this, ListStories.class);
		startActivityForResult(intent, AppConstants.ACTIVITY_STORIESLIST);
	}

	private void launchPMList() {
		Intent intent = new Intent(this, ListPM.class);
		startActivityForResult(intent, AppConstants.ACTIVITY_PMLIST);
	}

	private void launchMusicList() {
		Intent intent = new Intent(this, ListMusic.class);
		startActivityForResult(intent, AppConstants.ACTIVITY_MUSICLIST);
	}

	private void launchAccountActivity() {
		Intent intent = new Intent(this, AccountActivity.class);
		startActivityForResult(intent, AppConstants.ACTIVITY_SETTINGS);
	}

	private void launchChat() {
		Intent intent = new Intent(this, ChatActivity.class);
		startActivity(intent);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.i("SF MainMenu", "onActivityResult");
		checkButtonDisabledState();

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
	
	private void checkButtonDisabledState() {
		if (Authentication.getUsername() == null || Authentication.getUsername().trim().length() <= 0 ||
				Authentication.getPassword() == null || Authentication.getPassword().trim().length() <= 0) {
				buttonPMs.setEnabled(false);
				buttonChat.setEnabled(false);
		}
		else
		{
			buttonPMs.setEnabled(true);
			buttonChat.setEnabled(true);
		}
	}

}
