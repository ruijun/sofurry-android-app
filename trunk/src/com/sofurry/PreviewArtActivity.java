package com.sofurry;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.sofurry.util.Authentication;
import com.sofurry.util.IconStorage;

public class PreviewArtActivity extends Activity {

	ImageView image;
	TextView imageartisttext;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Authentication.loadAuthenticationInformation(this);
		setContentView(R.layout.artdetails);
		
		image = (ImageView) findViewById(R.id.imagepreview);
		imageartisttext = (TextView) findViewById(R.id.imageartisttext);

		Bundle extras = getIntent().getExtras() ;
	    if( extras != null ) {
	        int pageID = extras.getInt("pageID");
	        String name = extras.getString("name");
	        String tags = extras.getString("tags");
	        String authorName = extras.getString("authorName");
	        String authorId = extras.getString("authorId");
	        
			Bitmap thumb = IconStorage.loadSubmissionIcon(pageID);
			if (thumb != null)
				image.setImageBitmap(thumb);
			
			imageartisttext.setText(authorName);
	    }

	}



}
