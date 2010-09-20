package com.sofurry.itemviews;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sofurry.AppConstants;
import com.sofurry.FavableActivity;
import com.sofurry.ProgressBarHelper;
import com.sofurry.R;
import com.sofurry.util.ContentDownloader;
import com.sofurry.util.FileStorage;
import com.sofurry.util.HttpRequest;
import com.sofurry.util.ImageStorage;

public class PreviewArtActivity extends FavableActivity implements Runnable {

	private ProgressBarHelper pbh = new ProgressBarHelper(this);
	private ImageView image;
	private TextView imageartisttext;
	
	private Button hdButton = null;
    
    String tags;
    String authorName;
    int authorId;
    String thumbnailUrl;
    
    private String filename = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Authentication.loadAuthenticationInformation(this);
		setContentView(R.layout.artdetails);
		
		image = (ImageView) findViewById(R.id.imagepreview);
		imageartisttext = (TextView) findViewById(R.id.imageartisttext);
		
	    hdButton = (Button) findViewById(R.id.HDViewButton);
	    hdButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				doHdView();
			}});
	    hdButton.setEnabled(false);

		Bundle extras = getIntent().getExtras() ;
	    if( extras != null ) {
	        name = extras.getString("name");
	        tags = extras.getString("tags");
	        authorName = extras.getString("authorName");
	        authorId = extras.getInt("authorId");
	        thumbnailUrl = extras.getString("thumbnail");
	        
			Bitmap thumb = ImageStorage.loadSubmissionIcon(pageID);
			if (thumb != null)
				image.setImageBitmap(thumb);
			
			filename = name + HttpRequest.extractExtension(thumbnailUrl);
			
			imageartisttext.setText(name + "\n" + authorName);
	    }

	    pbh.showProgressDialog("Fetching image...");
		Thread thread = new Thread(this);
		thread.start();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * Fetches an image, either from the icon storage
	 * 
	 */
	public void run() {
		try {
			String url = thumbnailUrl.replace("/thumbnails/", "/preview/");
			Log.i("SF ImageDownloader", "Downloading image for id " + pageID + " from " + url);
			
			Bitmap b = ImageStorage.loadSubmissionImage(filename);
			if (b == null) {
				// 1. extract extension
				String ext = HttpRequest.extractExtension(url);
				// 2. download file
				ContentDownloader.downloadFile(url, ImageStorage.getSubmissionImagePath(filename) );
				// 3. read file
				b = ImageStorage.loadSubmissionImage(filename);
				//b = ContentDownloader.downloadBitmap(url);
				//ImageStorage.saveSubmissionImage(pageID, b);
				if (b == null) throw new Exception("Downloaded Image failed to load.");
			}

			// Send bitmap to our hungry thread
			requesthandler.postMessage(0,b);
		} catch (Exception e) {
			requesthandler.postMessage(e);
		}
	}

	@Override
	public void createExtraMenuOptions(Menu menu) {
		menu.add(0,AppConstants.MENU_HD     ,0,"HD View").setIcon(android.R.drawable.ic_menu_gallery);
		super.createExtraMenuOptions(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_HD:
			doHdView();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	/**
	 * Saves the file to the images folder
	 */
	public void save() {
		try {
			
			File f = new File(FileStorage.getPath(ImageStorage.getSubmissionImagePath(filename)));
			if (!f.exists()) return; // Until that file exists, there is nothing we can do really.
			
			String targetPath = FileStorage.getUserStoragePath("Images", filename);
			File tf = new File(targetPath);
			
			FileStorage.ensureDirectory(tf.getParent());
			
			FileStorage.copyFile(f, tf);
			
			Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
			
		} catch (Exception e) {
			sonError(-1, e);
		}
		
	}
	
	/**
	 * Views the button using the acitivty that is associated with images
	 */
	public void doHdView() {
		File f = new File(FileStorage.getPath(ImageStorage.getSubmissionImagePath(filename)));
		if (!f.exists()) return; // Until that file exists, there is nothing we can do really.

		// Starts the associated image viewer, so the user can zoom and tilt
		Intent intent = new Intent();  
		intent.setAction(android.content.Intent.ACTION_VIEW);  
		intent.setDataAndType(Uri.fromFile(f), "image/*");  
		startActivity(intent);   
	}
	
	@Override
	public void sonOther(int id, Object obj) throws Exception {
		pbh.hideProgressDialog();
		// If the returntype is bitmap, we know what to do with it
		if (Bitmap.class.isAssignableFrom(obj.getClass())) {
		  image.setImageBitmap((Bitmap)obj);
		  hdButton.setEnabled(true);
		}
		else
		  super.sonOther(id, obj);
	}

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#finish()
	 */
	@Override
	public void finish() {
		super.finish();
	}
	
}
