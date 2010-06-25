package com.sofurry;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.sofurry.util.Authentication;
import com.sofurry.util.ContentDownloader;
import com.sofurry.util.IconStorage;

public class PreviewArtActivity extends Activity implements Runnable {

	private ProgressDialog pd;
	private ImageView image;
	private TextView imageartisttext;
    int pageID;
    String name;
    String tags;
    String authorName;
    String authorId;
    String thumbnailUrl;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Authentication.loadAuthenticationInformation(this);
		setContentView(R.layout.artdetails);
		
		image = (ImageView) findViewById(R.id.imagepreview);
		imageartisttext = (TextView) findViewById(R.id.imageartisttext);

		Bundle extras = getIntent().getExtras() ;
	    if( extras != null ) {
	        pageID = extras.getInt("pageID");
	        name = extras.getString("name");
	        tags = extras.getString("tags");
	        authorName = extras.getString("authorName");
	        authorId = extras.getString("authorId");
	        thumbnailUrl = extras.getString("thumbnail");
	        
			Bitmap thumb = IconStorage.loadSubmissionIcon(pageID);
			if (thumb != null)
				image.setImageBitmap(thumb);
			
			imageartisttext.setText(authorName);
	    }

		pd = ProgressDialog.show(this, "Fetching image...", "Please wait", true, false);
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void run() {
		String url = thumbnailUrl.replace("/thumbnails/", "/preview/");
		Log.i("SF ImageDownloader", "Downloading image for id " + pageID + " from " + url);
		Bitmap b = IconStorage.loadSubmissionImage(pageID);
		if (b == null) {
			b = ContentDownloader.downloadBitmap(url);
			IconStorage.saveSubmissionImage(pageID, b);
		}
		Message msg = handler.obtainMessage();
		msg.obj = b;
		handler.sendMessage(msg);

	}

	// Separate handler to let android update the view whenever possible
	protected Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			try {
			pd.dismiss();
				if (msg.obj != null) {
					Bitmap b = (Bitmap) msg.obj;
					image.setImageBitmap(b);
				}
			} catch (Exception e) {
				Log.e("SF", "Exception in PreviewArtActivity Handler", e);
			}
		}
	};


}
