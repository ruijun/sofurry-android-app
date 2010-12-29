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
import com.sofurry.R;
import com.sofurry.base.classes.FavableActivity;
import com.sofurry.util.ContentDownloader;
import com.sofurry.util.FileStorage;
import com.sofurry.util.HttpRequest;
import com.sofurry.util.ImageStorage;

public class ViewArtActivity extends FavableActivity implements Runnable {

	private ImageView image;
	private TextView imageartisttext;
	private Thread imageFetcher = null;
	private Button hdButton = null;
    private String content = null;
	
    private String tags;
    private String authorName;
    private String thumbnailUrl;
    private Bitmap imageBitmap = null;
    
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

	    if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras() ;
		    if( extras != null ) {
		        name = extras.getString("name");
		        tags = extras.getString("tags");
		        authorName = extras.getString("authorName");
		        thumbnailUrl = extras.getString("thumbnail");
		        
				Bitmap thumb = ImageStorage.loadSubmissionIcon(pageID);
				if (thumb != null)
					image.setImageBitmap(thumb);
				
				filename = name + HttpRequest.extractExtension(thumbnailUrl);
				filename = FileStorage.sanitize(filename);
				
				content = name + "\n" + authorName;
		    }
	
			imageFetcher = new Thread(this);
			imageFetcher.start();
	    } else {
	    	content = (String) retrieveObject("content");
	    	filename = (String) retrieveObject("filename");
	    	imageBitmap = (Bitmap) retrieveObject("image");
	    	imageFetcher = (Thread) retrieveObject("thread"); 
	    }
    	if (imageFetcher != null)
	      pbh.showProgressDialog("Fetching image...");
    	if (imageBitmap != null) 
    	  showImage();
		imageartisttext.setText(content);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		storeObject("content", content);
		storeObject("filename", filename);
		storeObject("image", imageBitmap);
		storeObject("thread", imageFetcher);
		super.onSaveInstanceState(outState);
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
				ContentDownloader.downloadFile(url, ImageStorage.getSubmissionImagePath(filename) , null);
				// 3. read file
				b = ImageStorage.loadSubmissionImage(filename);
				//b = ContentDownloader.downloadBitmap(url);
				//ImageStorage.saveSubmissionImage(pageID, b);
				if (b == null) throw new Exception("Downloaded Image failed to load.");
			}

			// Send bitmap to our hungry thread
			requesthandler.postMessage(AppConstants.REQUEST_ID_DOWNLOADIMAGE,b);
		} catch (Exception e) {
			requesthandler.postMessage(AppConstants.REQUEST_ID_DOWNLOADIMAGE,e);
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
			if (filename == null) throw new Exception("File has not downloaded properly yet. Filename is null.");
			File f = new File(FileStorage.getPath(ImageStorage.getSubmissionImagePath(filename)));
			if (!f.exists()) throw new Exception("File has not downloaded properly yet. File does not exist.");
			
			String targetPath = FileStorage.getUserStoragePath("Images", filename);
			File tf = new File(targetPath);
			
			FileStorage.ensureDirectory(tf.getParent());
			
			FileStorage.copyFile(f, tf);
			
			Toast.makeText(getApplicationContext(), "File saved to:\n" + targetPath, Toast.LENGTH_LONG).show();
			
		} catch (Exception e) {
			onError(-1, e);
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
	public void onOther(int id, Object obj) throws Exception {
		pbh.hideProgressDialog();
		// If the returntype is bitmap, we know what to do with it
		if (id == AppConstants.REQUEST_ID_DOWNLOADIMAGE) {
			imageBitmap = (Bitmap)obj;
			showImage();
		  imageFetcher = null;
		}
		else
		  super.onOther(id, obj);
	}
	
	/**
	 * Shows the image
	 */
	public void showImage() {
		  image.setImageBitmap(imageBitmap);
		  hdButton.setEnabled(true);
	}
	
	@Override
	public void onError(int id, Exception e) {
		if (id == AppConstants.REQUEST_ID_DOWNLOADIMAGE) imageFetcher = null;
		super.onError(id, e);
	}

	/* (non-Javadoc)
	 * @see com.sofurry.IManagedActivity#finish()
	 */
	@Override
	public void finish() {
		super.finish();
	}
	
	
	
}
