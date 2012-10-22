package com.sofurry.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.adapters.SubmissionGalleryAdapter;
import com.sofurry.adapters.SubmissionListAdapter;
import com.sofurry.base.classes.AbstractBrowseActivity;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.SFSubmissionList;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.downloadmanager.DownloadManager;
import com.sofurry.mobileapi.downloadmanager.HTTPFileDownloadTask;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;
import com.sofurry.storage.ImageStorage;
import com.sofurry.util.Utils;

/**
 * Browse SoFurry submissions
 * @author Night_Gryphon
 *
 */
public class SFBrowseSubmissionActivity extends AbstractBrowseActivity<Submission> {

	protected ContentType fContentType = ContentType.all;
	protected ViewSource fContentFilter = ViewSource.all;
	protected String fExtra = "";
	protected String fTitle = null;
	protected int fAuthorId = -1;
	
	private AsyncTask<Integer, Integer, Bitmap> iconLoader = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			fContentType = (ContentType) savedInstanceState.getSerializable("ContentType");
			fContentFilter = (ViewSource) savedInstanceState.getSerializable("ContentFilter");
			fExtra = savedInstanceState.getString("Extra");
			fTitle = savedInstanceState.getString("activityTitle");
			fAuthorId = savedInstanceState.getInt("AuthorId", -1);
		} else {
		    Bundle extras = getIntent().getExtras();
		    if (extras != null) {
				fContentType = (ContentType) extras.getSerializable("ContentType");
				fContentFilter = (ViewSource) extras.getSerializable("ContentFilter");
				fExtra = extras.getString("Extra");
				fTitle = extras.getString("activityTitle");
				fAuthorId = extras.getInt("AuthorId", -1);
		    }
		}
		
		if (fContentType == null)
			fContentType = ContentType.all;
		
		if (fContentFilter == null)
			fContentFilter = ViewSource.all;

		// check this with each of fContentType layouts. Now it is compatible with any used layout.
		if (fContentFilter == ViewSource.user)
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		switch (fContentType) {
		case art:
			setContentView(R.layout.gallerylayout);
			setTitle("Browse Art");
	        SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(this);
			((GridView) getDataView()).setColumnWidth(Utils.dp_to_px(this, prefs.getInt(AppConstants.PREFERENCE_THUMB_SIZE, 130) ));

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
		
		// set artist info bar for browse user
		if (fContentFilter == ViewSource.user) {
			LinearLayout mainlayout = (LinearLayout) findViewById(R.id.main_layout);
			if (mainlayout != null) {
				LayoutInflater mInflater = LayoutInflater.from(this);
		        View infoview = mInflater.inflate(R.layout.artist_info_bar, null);
		        mainlayout.addView(infoview, 0);
		        
		        ((TextView) infoview.findViewById(R.id.AuthorName)).setText(fTitle);
		        ((ImageView) infoview.findViewById(R.id.btnBack)).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						finish();						}
				});
		        
		        if (fAuthorId >= 0) {
		        	iconLoader = new AsyncTask<Integer, Integer, Bitmap>() {

						@Override
						protected Bitmap doInBackground(Integer... params) {
							try {
								return ApiFactory.getUserIcon(params[0]);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
		        		
						protected void onPostExecute(Bitmap result) {
							if (! isCancelled()) {
								ImageView iconview = (ImageView) findViewById(R.id.AuthorIcon);
								if (iconview != null)
									iconview.setImageBitmap(result);
							}
					    }
					};
		        
					iconLoader.execute(new Integer(fAuthorId));
		        }
			}
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
		outState.putString("activityTitle", fTitle);
		outState.putInt("AuthorId", fAuthorId);
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

        SubMenu sub = menu.addSubMenu(0,0,20,"More").setIcon(android.R.drawable.ic_menu_more);
        sub.add(0, AppConstants.MENU_LOAD_THUMB, 10, "Load missing thumnails");
        sub.add(0, AppConstants.MENU_PRELOAD, 10, "Cache all");
//      sub.add(0, AppConstants.MENU_DOWNLOAD_ALL, 10, "Download all");

        return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case AppConstants.MENU_PRELOAD:
			Integer i = Integer.parseInt(Utils.getPreferences(this).getString(AppConstants.PREFERENCE_PRELOAD_MAX, "200"));
			if (i != null)
				PreloadItems(i);
			return true;
			
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

		case AppConstants.MENU_LOAD_THUMB:
			((SFSubmissionList) fList).RefreshThumbnails();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
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

	// ========================= PRELOAD =======================
	private DownloadManager dlmanager = new DownloadManager(4); //TODO number of threads setting
//	private NotificationHelper notification = new NotificationHelper(this); 
	private int dlindex = -1;

	/**
	 * Pass download tasks to preload to dl manager for items from startItemIndex till the end of loaded list
	 * @param dlManager - manager to get download tasks
	 * @param startItemIndex - item to start processing from
	 * @return - index of item to start processing from in next feedLoader call
	 */
	protected int feedPreloader(DownloadManager dlManager, int startItemIndex) {
		Submission s = null;
		int i = startItemIndex;
		do {
			s = fList.get(i, false);
			if (s == null)
				break;
			if (! s.isSubmissionFileExists())
				dlManager.Download(new HTTPFileDownloadTask(
						s.getFullURL(), ImageStorage.getSubmissionImagePath(s.getCacheName()),
						null, 15, false, true, null, "text", 3));
			
			i++;
		} while (s != null);
		return i; 
	}
	
	public void PreloadItems(final int numItems) {
		if (fList == null)
			return;

		dlmanager.setNumThreads(Utils.getPreferences(this).getInt(AppConstants.PREFERENCE_PRELOAD_THREADS, 4));
		
		if (dlindex <0) {

			// feed downloader with already loaded items
			dlindex = feedPreloader(dlmanager, 0);
			
			fList.setStatusListener(new IJobStatusCallback() {
				public void onSuccess(Object job) {
					refreshDataView();
					onLoadFinish();
					setListCallback(); // reset callback to default
					
					dlindex = feedPreloader(dlmanager, dlindex); // feed rest of items
					setListCallback(); // restore callback
					dlindex = -1; // unlock new preloads
				}
				
				public void onStart(Object job) {
					onLoadStart();
				}
				
				// Called by SFSubmissionList on thumb loading progress
				public void onProgress(Object job, int progress, int total, String msg) {
					if (job instanceof NetworkList) { // progress from load page, skip thumb progress
						Log.d("[Preload]", "Item "+progress+" of "+total+"done");
						dlindex = feedPreloader(dlmanager, dlindex); // feed downloader on page loaded
					}
					refreshDataView();
				}
				
				public void onError(Object job, String msg) {
					onLoadError(msg);
					dlindex = feedPreloader(dlmanager, dlindex); // feed rest of items
					setListCallback();
					dlindex = -1;
				}
			});
		}
		

		if (fList.sizeLoaded() < numItems)
			fList.PreloadItems(numItems);
		
	}
	

	
}
