package com.sofurry.base.classes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.activities.TagEditorActivity;
import com.sofurry.activities.ViewArtActivity;
import com.sofurry.activities.ViewJournalActivity;
import com.sofurry.activities.ViewMusicActivity;
import com.sofurry.activities.ViewStoryActivity;
import com.sofurry.adapters.SubmissionGalleryAdapter;
import com.sofurry.adapters.SubmissionListAdapter;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.SFSubmissionList;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;

/**
 * Browse SoFurry submissions
 * @author Night_Gryphon
 *
 */
public class SFBrowseActivity extends AbstractBrowseActivity {

	protected ContentType fContentType = ContentType.all;
	protected ViewSource fContentFilter = ViewSource.all;
	protected String fExtra = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String fTitle = null;
		
		if (savedInstanceState != null) {
			fContentType = (ContentType) savedInstanceState.getSerializable("ContentType");
			fContentFilter = (ViewSource) savedInstanceState.getSerializable("ContentFilter");
			fExtra = savedInstanceState.getString("Extra");
		} else {
		    Bundle extras = getIntent().getExtras();
		    if (extras != null) {
				fContentType = (ContentType) extras.getSerializable("ContentType");
				fContentFilter = (ViewSource) extras.getSerializable("ContentFilter");
				fExtra = extras.getString("Extra");
				fTitle = extras.getString("activityTitle");
		    }
		}
		
		if (fContentType == null)
			fContentType = ContentType.all;
		
		if (fContentFilter == null)
			fContentFilter = ViewSource.all;
		
		switch (fContentType) {
		case art:
			setContentView(R.layout.gallerylayout);
			setTitle("Browse Art");
	        SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(this);
			((GridView) getDataView()).setColumnWidth(prefs.getInt(AppConstants.PREFERENCE_THUMB_SIZE, 130));

			break;

		case music:
			setContentView(R.layout.listlayout);
			setTitle("Browse Music");
			break;
			
		case journals:
			setContentView(R.layout.listlayout);
			setTitle("Browse Journals");
			break;
			
		case stories:
			setContentView(R.layout.listlayout);
			setTitle("Browse Stories");
			break;
			
		default:
			break;
		} 
		
		if (fTitle != null)
			setTitle(fTitle);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("ContentType", fContentType);
		outState.putSerializable("ContentFilter", fContentFilter);
		outState.putString("Extra", fExtra);
	}

	@Override
	protected AdapterView getDataView() {
		switch (fContentType) {
		case art:
			return (AdapterView) findViewById(R.id.galleryview);

		case music:
			return (AdapterView) findViewById(R.id.list_view);
			
		case journals:
			return (AdapterView) findViewById(R.id.list_view);
			
		case stories:
			return (AdapterView) findViewById(R.id.list_view);
			
		default:
			return null;
		} 
	}

	@Override
	protected Adapter createAdapter() {
		switch (fContentType) {
		case art:
			return new SubmissionGalleryAdapter(this, fList);

		case music:
			return new SubmissionListAdapter(this, R.layout.listitemtwolineicon, fList);
			
		case journals:
			return new SubmissionListAdapter(this, R.layout.listitemtwolineicon, fList);
			
		case stories:
			return new SubmissionListAdapter(this, R.layout.listitemtwolineicon, fList);
			
		default:
			return null;
		} 
	}

	@Override
	protected NetworkList<Submission> createBrowseList() {
		SFSubmissionList list = new SFSubmissionList(fContentFilter, fExtra, fContentType);
		list.preloadCount = AppConstants.ENTRIESPERPAGE_GALLERY / 4;
		return list;
	}

	// this is SF specific menus	
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		SubMenu viewSourceMenu = menu.addSubMenu("Filter & Search").setIcon(android.R.drawable.ic_menu_search);
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_KEYWORDS, 0, "Keywords");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_ALL, 0, "All Submissions");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_FEATURED, 0, "Featured");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_FAVORITES, 0, "Your Favorites");
//		viewSourceMenu.add(0, AppConstants.MENU_FILTER_WATCHLIST, 0, "Watchlist");
		viewSourceMenu.add(0, AppConstants.MENU_FILTER_GROUP, 0, "Your Groups");
//		viewSourceMenu.add(0, AppConstants.MENU_FILTER_WATCHLIST_COMBINED, 0, "Watches + Groups");
		//menu.add(0, AppConstants.MENU_FILTER_KEYWORDS, 0, "Keywords");

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_FILTER_KEYWORDS:
			Intent intent = new Intent(this, TagEditorActivity.class);
			startActivityForResult(intent, AppConstants.ACTIVITY_TAGS);
			return true;

		case AppConstants.MENU_FILTER_ALL:
			setTitle("Recent");
			fContentFilter = ViewSource.all;
			fExtra = "";
			setList(createBrowseList());
			return true;

		case AppConstants.MENU_FILTER_FEATURED:
			setTitle("Featured");
			fContentFilter = ViewSource.featured;
			fExtra = ""+ApiFactory.myUserProfile.userID;
			setList(createBrowseList());
			return true;

		case AppConstants.MENU_FILTER_FAVORITES:
			setTitle("Favorites");
			fContentFilter = ViewSource.favorites;
			fExtra = ""+ApiFactory.myUserProfile.userID;
			setList(createBrowseList());
			return true;

		case AppConstants.MENU_FILTER_WATCHLIST:
			setTitle("Watchlist");
			fContentFilter = ViewSource.watchlist;
			fExtra = ""+ApiFactory.myUserProfile.userID;
			setList(createBrowseList());
			return true;
		/*
		case AppConstants.MENU_FILTER_GROUP:
			currentTitle = "Group";
			resetViewSource(ViewSource.group);
			return true; // not supported anymore
			*/
		case AppConstants.MENU_FILTER_WATCHLIST_COMBINED:
			setTitle("Combined");
			fContentFilter = ViewSource.combinedwatch;
			fExtra = ""+ApiFactory.myUserProfile.userID;
			setList(createBrowseList());
			return true;

		default:
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Handles the return value from TAGEditor
		if ((requestCode == AppConstants.ACTIVITY_TAGS) && (data != null)) {
			setTitle("Tags");
			fContentFilter = ViewSource.search;
			fExtra = data.getStringExtra("tags");
			setList(createBrowseList());
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDataViewItemClick(int aItemIndex) {
		if ( (fList != null) && (fList.get(aItemIndex) != null)) {
			Submission s = fList.get(aItemIndex);
			Intent i = null;
			
				
			switch (s.getType()) {
			case art:
				Log.i(AppConstants.TAG_STRING, "SFGallery: Viewing art ID: " + s.getId());
				i = new Intent(this, ViewArtActivity.class);
				s.feedIntent(i);
				// allow viewer to know submissions list
//				fList.setStatusListener(null);
				i.putExtra("listId", fList.getListId()); 
				i.putExtra("listIndex", aItemIndex); 
				if (fContentFilter == ViewSource.user)
					i.putExtra("NoMoreFromUserButton", true);
				startActivityForResult(i, 0);
				break;
				
			case journals:
				Log.i(AppConstants.TAG_STRING, "ListJournals: Viewing journal ID: " + s.getId());
				i = new Intent(this, ViewJournalActivity.class);
				s.feedIntent(i);
				if (fContentFilter == ViewSource.user)
					i.putExtra("NoMoreFromUserButton", true);
				startActivityForResult(i, 0);
				break;
				
			case music:
				Log.i(AppConstants.TAG_STRING, "ListMusic: Viewing music ID: " + s.getId());
				i = new Intent(this, ViewMusicActivity.class);
				s.feedIntent(i);
				if (fContentFilter == ViewSource.user)
					i.putExtra("NoMoreFromUserButton", true);
				startActivityForResult(i, 0);
				break;
				
			case stories:
				Log.i(AppConstants.TAG_STRING, "ListStories: Viewing story ID: " + s.getId());
				i = new Intent(this, ViewStoryActivity.class);
				s.feedIntent(i);
				if (fContentFilter == ViewSource.user)
					i.putExtra("NoMoreFromUserButton", true);
				startActivityForResult(i, 0);
				break;
			}
		}
		super.onDataViewItemClick(aItemIndex);
	}

	
}
