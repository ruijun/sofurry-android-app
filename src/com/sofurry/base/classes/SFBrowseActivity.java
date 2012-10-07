package com.sofurry.base.classes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Adapter;
import android.widget.AdapterView;
import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.activities.TagEditorActivity;
import com.sofurry.adapters.SubmissionGalleryAdapter;
import com.sofurry.adapters.SubmissionListAdapter;
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
		return new SFSubmissionList(fContentFilter, fExtra, fContentType);
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
			setList(createBrowseList());
			return true;
		case AppConstants.MENU_FILTER_FEATURED:
			setTitle("Featured");
			fContentFilter = ViewSource.featured;
			setList(createBrowseList());
			return true;
		case AppConstants.MENU_FILTER_FAVORITES:
			setTitle("Favorites");
			fContentFilter = ViewSource.favorites;
			setList(createBrowseList());
			return true;
		case AppConstants.MENU_FILTER_WATCHLIST:
			setTitle("Watchlist");
			fContentFilter = ViewSource.watchlist;
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

}
