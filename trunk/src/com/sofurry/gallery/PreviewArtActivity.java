package com.sofurry.gallery;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sofurry.ActivityWithRequests;
import com.sofurry.AppConstants;
import com.sofurry.ProgressBarHelper;
import com.sofurry.R;
import com.sofurry.RateActivity;
import com.sofurry.TagEditor;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.ContentDownloader;
import com.sofurry.util.FileStorage;
import com.sofurry.util.ImageStorage;

public class PreviewArtActivity extends ActivityWithRequests implements Runnable {

	private ProgressBarHelper pbh = new ProgressBarHelper(this);
	private ImageView image;
	private TextView imageartisttext;
	
	private Button hdButton = null;
    
	int pageID;
    String name;
    String tags;
    String authorName;
    int authorId;
    String thumbnailUrl;

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
	        pageID = extras.getInt("pageID");
	        name = extras.getString("name");
	        tags = extras.getString("tags");
	        authorName = extras.getString("authorName");
	        authorId = extras.getInt("authorId");
	        thumbnailUrl = extras.getString("thumbnail");
	        
			Bitmap thumb = ImageStorage.loadSubmissionIcon(pageID);
			if (thumb != null)
				image.setImageBitmap(thumb);
			
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
			
			Bitmap b = ImageStorage.loadSubmissionImage(pageID);
			if (b == null) {
				b = ContentDownloader.downloadBitmap(url);
				ImageStorage.saveSubmissionImage(pageID, b);
			}

			// Send bitmap to our hungry thread
			requesthandler.postMessage(0,b);
		} catch (Exception e) {
			requesthandler.postMessage(e);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Creates the Context Menu for this Activity.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0,AppConstants.MENU_HD     ,0,"HD View");
		menu.add(0,AppConstants.MENU_ADDFAV ,0,"Add Fav");
		menu.add(0,AppConstants.MENU_REMFAV ,0,"Remove Fav");
		menu.add(0,AppConstants.MENU_RATE   ,0,"Rate");
		//SubMenu rate = menu.addSubMenu("Rate");
//		rate.add(0, AppConstants.MENU_RATE1 ,0,"1 Star");
//		rate.add(0, AppConstants.MENU_RATE2 ,0,"2 Star");
//		rate.add(0, AppConstants.MENU_RATE3 ,0,"3 Star");
//		rate.add(0, AppConstants.MENU_RATE4 ,0,"4 Star");
//		rate.add(0, AppConstants.MENU_RATE5 ,0,"5 Star");
		menu.add(0, AppConstants.MENU_CUM   ,0,"Cum!");
		
		return result;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_HD:
			doHdView();
			return true;
		case AppConstants.MENU_ADDFAV:
			setFavorite();
			return true;
		case AppConstants.MENU_REMFAV:
			unsetFavorite();
			return true;
		case AppConstants.MENU_RATE:
			Intent intent = new Intent(this, RateActivity.class);
			startActivityForResult(intent, AppConstants.ACTIVITY_RATE);
			return true;
//		case AppConstants.MENU_RATE1:
//			setRating(1);
//			return true;
//		case AppConstants.MENU_RATE2:
//			setRating(2);
//			return true;
//		case AppConstants.MENU_RATE3:
//			setRating(3);
//			return true;
//		case AppConstants.MENU_RATE4:
//			setRating(4);
//			return true;
//		case AppConstants.MENU_RATE5:
//			setRating(5);
//			return true;
		case AppConstants.MENU_CUM:
			cum();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	/**
	 * Sets a Favorite for the currently Selected Image
	 */
	public void setFavorite() {
		pbh.showProgressDialog("Setting favorite");

		AjaxRequest request = new AjaxRequest();
		request.addParameter("f", "addfav");
		request.addParameter("pid", "" + pageID);
		request.execute(requesthandler);
	}
	
	/**
	 * Removes a Favorite for the currently Selected Image
	 */
	public void unsetFavorite() {
		pbh.showProgressDialog("Removing favorite");
		
		AjaxRequest request = new AjaxRequest();
		request.addParameter("f", "remfav");
		request.addParameter("pid", "" + pageID);
		request.execute(requesthandler);
	}
	
	/**
	 * Is forwarded from the activity, to handle callbacks of the TagEditor
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @return
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Handles the return value from TAGEditor
		if (requestCode == AppConstants.ACTIVITY_RATE) {
			if (data == null) return;
			int rating = data.getIntExtra("rating", -1);
			if (rating != -1) setRating(rating);
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * Sets the number of stars on a submission that the user wants to set
	 * @param stars
	 * The number of stars to set (1-5)
	 */
	public void setRating(int stars) {
		pbh.showProgressDialog("Rating "+stars+" stars");
		AjaxRequest request = new AjaxRequest();
		request.addParameter("f", "vote");
		request.addParameter("pid", "" + pageID);
		request.addParameter("votevalue", "" + stars);
		request.execute(requesthandler);
	}

	/**
	 * Flags the cum-counter for currently visible image
	 */
	public void cum() {
		pbh.showProgressDialog("Cumming ...");
		AjaxRequest request = new AjaxRequest();
		request.addParameter("f", "cum");
		request.addParameter("pid", "" + pageID);
		request.execute(requesthandler);
	}
	
	
	/**
	 * Views the button using the acitivty that is associated with images
	 */
	public void doHdView() {
		File f = new File(FileStorage.getPath(ImageStorage.getSubmissionImagePath(pageID)));
		if (!f.exists()) return; // Until that file exists, there is nothing we can do really.

		// Starts the associated image viewer, so the user can zoom and tilt
		Intent intent = new Intent();  
		intent.setAction(android.content.Intent.ACTION_VIEW);  
		intent.setDataAndType(Uri.fromFile(f), "image/*");  
		startActivity(intent);   
	}
	
	@Override
	public void sonError(int id, Exception e) {
		super.sonError(id, e);
		
		pbh.hideProgressDialog();
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



}
