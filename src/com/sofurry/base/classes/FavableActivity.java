package com.sofurry.base.classes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.sofurry.AppConstants;
import com.sofurry.activities.GalleryArtActivity;
import com.sofurry.activities.ListMusicActivity;
import com.sofurry.activities.ListStoriesActivity;
import com.sofurry.activities.RateActivity;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.requests.AndroidRequestWrapper;
import com.sofurry.requests.DataCall;

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
		SubMenu favsmenu = menu.addSubMenu("Favs & More").setIcon(android.R.drawable.ic_menu_add);
		favsmenu.add(0,AppConstants.MENU_ADDFAV ,0,"Add Fav").setIcon(android.R.drawable.ic_menu_add);
		favsmenu.add(0,AppConstants.MENU_REMFAV ,0,"Remove Fav").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		favsmenu.add(0,AppConstants.MENU_RATE   ,0,"Rate").setIcon(android.R.drawable.btn_star_big_off);
		favsmenu.add(0, AppConstants.MENU_CUM   ,0,"Cum!").setIcon(android.R.drawable.ic_menu_compass);
		SubMenu usermenu = menu.addSubMenu("Author").setIcon(android.R.drawable.ic_menu_more);
		usermenu.add(0, AppConstants.MENU_WATCH   ,0,"Watch").setIcon(android.R.drawable.ic_menu_search);
		usermenu.add(0, AppConstants.MENU_UNWATCH   ,0,"Unwatch").setIcon(android.R.drawable.ic_menu_delete);
		usermenu.add(0,AppConstants.MENU_USERSSTORIES,0,"Author's Stories").setIcon(android.R.drawable.ic_menu_slideshow);
		usermenu.add(0,AppConstants.MENU_USERSART,0,"Author's Art").setIcon(android.R.drawable.ic_menu_slideshow);
		usermenu.add(0,AppConstants.MENU_USERSMUSIK,0,"Author's Music").setIcon(android.R.drawable.ic_menu_slideshow);
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
		case AppConstants.MENU_WATCH:
			watch();
			return true;
		case AppConstants.MENU_UNWATCH:
			unwatch();
			return true;
		case AppConstants.MENU_USERSSTORIES:
			morefromuser(ListStoriesActivity.class,AppConstants.ACTIVITY_STORIESLIST);
			return true;
		case AppConstants.MENU_USERSART:
			morefromuser(GalleryArtActivity.class,AppConstants.ACTIVITY_GALLERYART);
			return true;
		case AppConstants.MENU_USERSMUSIK:
			morefromuser(ListMusicActivity.class,AppConstants.ACTIVITY_MUSICLIST);
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

		Request req = ApiFactory.createAddFav(pageID);
		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
		arw.exec(new DataCall() { public void call() { justHideProgress();	} });
	}
	
	/**
	 * Removes a Favorite for the currently Selected Image
	 */
	public void unsetFavorite() {
		pbh.showProgressDialog("Removing favorite");

		Request req = ApiFactory.createRemFav(pageID);
		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
		arw.exec(new DataCall() { public void call() { justHideProgress();	} });
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
		
		Request req = ApiFactory.createSetStars(pageID, stars);
		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
		arw.exec(new DataCall() { public void call() { justHideProgress();	} });
	}

	/**
	 * Flags the cum-counter for currently visible image
	 */
	public void cum() {
		pbh.showProgressDialog("Cumming ...");

		Request req = ApiFactory.createCum(pageID);
		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
		arw.exec(new DataCall() { public void call() { justHideProgress();	} });
	}

	/**
	 * Watches the current user
	 */
	public void watch() {
		pbh.showProgressDialog("Watching ...");
		
		Request req = ApiFactory.createWatch(authorId);
		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
		arw.exec(new DataCall() { public void call() { justHideProgress();	} });
	}
	/**
	 * Watches the current user
	 */
	public void unwatch() {
		pbh.showProgressDialog("Unwatching ...");
		
		Request req = ApiFactory.createUnWatch(authorId);
		AndroidRequestWrapper arw = new AndroidRequestWrapper(requesthandler, req);
		arw.exec(new DataCall() { public void call() { justHideProgress();	} });
	}

	/**
	 * Shows more work by the specific user
	 */
	public void morefromuser(Class<?> activity, int ActivityID) {
/*		Intent intent = new Intent(this, activity);
		intent.putExtra("viewSource", ViewSource.user.name());
		intent.putExtra("viewSearch", "" + authorId  );
/**/
		Intent intent = new Intent(this, SFBrowseActivity.class);
		
		switch (ActivityID) {
		case AppConstants.ACTIVITY_GALLERYART:
	        intent.putExtra("ContentType", ContentType.art);
			break;

		case AppConstants.ACTIVITY_MUSICLIST:
	        intent.putExtra("ContentType", ContentType.music);
			break;

		case AppConstants.ACTIVITY_STORIESLIST:
	        intent.putExtra("ContentType", ContentType.stories);
			break;

		case AppConstants.ACTIVITY_JOURNALSLIST:
	        intent.putExtra("ContentType", ContentType.journals);
			break;

		default:
			return;
		}
		
		intent.putExtra("ContentFilter", ViewSource.user);
		intent.putExtra("Extra", "" + authorId);

		intent.putExtra("activityTitle", "" + authorName  );
		intent.putExtra("NoMoreFromUserButton", true);

		startActivityForResult(intent, ActivityID);
	}
	
	/**
	 * Method called by some requests, that do not handle the returned data.
	 */
	public void justHideProgress() {
		pbh.hideProgressDialog();
	}

}
