package com.sofurry;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.sofurry.requests.AjaxRequest;

/**
 * @author Rangarig
 * 
 * A class that contains all the tools for an item that is favable, ratable and you know. 
 * 
 */
public abstract class FavableActivity extends SubmissionViewActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void createExtraMenuOptions(Menu menu) {
		menu.add(0,AppConstants.MENU_ADDFAV ,0,"Add Fav").setIcon(android.R.drawable.ic_menu_add);
		menu.add(0,AppConstants.MENU_REMFAV ,0,"Remove Fav").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0,AppConstants.MENU_RATE   ,0,"Rate").setIcon(android.R.drawable.btn_star_big_off);
		menu.add(0, AppConstants.MENU_CUM   ,0,"Cum!").setIcon(android.R.drawable.ic_menu_compass);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
		case AppConstants.MENU_CUM:
			cum();
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
		request.setRequestID(AppConstants.REQUEST_ID_FAV);
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
		request.setRequestID(AppConstants.REQUEST_ID_UNFAV);
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
		request.setRequestID(AppConstants.REQUEST_ID_RATE);
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
		request.setRequestID(AppConstants.REQUEST_ID_CUM);
		request.execute(requesthandler);
	}

	@Override
	public void sonData(int id, JSONObject obj) throws Exception {
		switch (id) {
		case AppConstants.REQUEST_ID_CUM:
			pbh.hideProgressDialog();
			return;
		case AppConstants.REQUEST_ID_FAV:
			pbh.hideProgressDialog();
			return;
		case AppConstants.REQUEST_ID_UNFAV:
			pbh.hideProgressDialog();
			return;
		case AppConstants.REQUEST_ID_RATE:
			pbh.hideProgressDialog();
			return;
		}
		super.sonData(id, obj);
		
	}
	
	
	
}
