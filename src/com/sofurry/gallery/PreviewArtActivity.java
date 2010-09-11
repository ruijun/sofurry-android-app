package com.sofurry.gallery;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ImageView;
import android.widget.TextView;

import com.sofurry.ActivityWithRequests;
import com.sofurry.AppConstants;
import com.sofurry.ProgressBarHelper;
import com.sofurry.R;
import com.sofurry.requests.AjaxRequest;
import com.sofurry.util.Authentication;
import com.sofurry.util.ContentDownloader;
import com.sofurry.util.ImageStorage;

public class PreviewArtActivity extends ActivityWithRequests implements Runnable {

	private ProgressBarHelper pbh = new ProgressBarHelper(this);
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
		menu.add(0,AppConstants.MENU_ADDFAV,0,"Add Fav");
		menu.add(0,AppConstants.MENU_REMFAV,0,"Remove Fav");
		SubMenu rate = menu.addSubMenu("Rate");
		rate.add(0, AppConstants.MENU_RATE1, 0, "1 Star");
		rate.add(0, AppConstants.MENU_RATE2, 0, "2 Star");
		rate.add(0, AppConstants.MENU_RATE3, 0, "3 Star");
		rate.add(0, AppConstants.MENU_RATE4, 0, "4 Star");
		rate.add(0, AppConstants.MENU_RATE5, 0, "5 Star");
		menu.add(0, AppConstants.MENU_CUM,0,"Cum!");
		
		return result;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_ADDFAV:
			setFavorite();
			return true;
		case AppConstants.MENU_REMFAV:
			unsetFavorite();
			return true;
		case AppConstants.MENU_RATE1:
			setRating(1);
			return true;
		case AppConstants.MENU_RATE2:
			setRating(2);
			return true;
		case AppConstants.MENU_RATE3:
			setRating(3);
			return true;
		case AppConstants.MENU_RATE4:
			setRating(4);
			return true;
		case AppConstants.MENU_RATE5:
			setRating(5);
			return true;
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
	
	
	@Override
	public void sonOther(int id, Object obj) throws Exception {
		pbh.hideProgressDialog();
		// If the returntype is bitmap, we know what to do with it
		if (Bitmap.class.isAssignableFrom(obj.getClass()))
		  image.setImageBitmap((Bitmap)obj);
		else
		  super.sonOther(id, obj);
	}



}
