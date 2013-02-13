/**
 * 
 */
package com.sofurry.mobileapi;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IJobStatusCallback;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ParseBrowseResult;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.mobileapi.core.Request;
import com.sofurry.mobileapi.downloadmanager.DownloadManager;
import com.sofurry.mobileapi.downloadmanager.HTTPFileDownloadTask;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;
import com.sofurry.util.Utils;

/**
 * Implementation of SoFurry Submission List
 * Handle 'Browse' website API call
 * 
 * @author Night_Gryphon
 *
 */
public class SFSubmissionList extends NetworkList<Submission> {

	private int currentPage = -1;
	private int totalPages = -1;
	private boolean fFinalPage = false;
	
	private ViewSource fSource = ViewSource.all;
	private String fExtra = null;
	private ContentType fContentType = ContentType.all;

	private AsyncTask<Object, Integer, Integer> updateCacheTask = null;
	/**
	 * 
	 */
	public SFSubmissionList(ViewSource source, String extra, ContentType contentType) {
		super(0, true);
		fSource = source;
		fExtra = extra;
		fContentType = contentType;
		
		// run cache load in separate thread as it freeze UI thread
		(new AsyncTask<Object, Integer, ArrayList<Submission>>() {

			@Override
			protected ArrayList<Submission> doInBackground(Object... params) {
				Thread.currentThread().setName("Read cache "+((String) params[2]));
				return Utils.BrowseCache().getCache((ViewSource) params[0], (ContentType) params[1], (String) params[2]);
			}

			@Override
			protected void onPostExecute(ArrayList<Submission> result) {
				Cache = result;
				// notify cache done
				if (Cache != null)
					SFSubmissionList.super.doSuccessNotify(this); // call parent onSuccess as we don't want to update cache
			}
		}).execute(source, contentType, extra);
//		Cache = Utils.BrowseCache().getCache(source, contentType, extra);
	}

	@Override
	protected void doLoadNextPage(IJobStatusCallback StatusCallback) throws Exception {
			if (isFinalPage()) return; // don't fetch after last page
			
			Request req = ApiFactory.createBrowse(fSource, fExtra, fContentType, AppConstants.ENTRIESPERPAGE_GALLERY, currentPage+1);
			JSONObject res = req.execute();
			
			ParseBrowseResult loaded = ApiFactory.ParseBrowse(res, this);
			currentPage++; // set current page as loaded only on success
			totalPages = loaded.NumPages;
	}

	@Override
	public Boolean isFinalPage() {
		return (totalPages >= 0) && (currentPage >= totalPages-1);
	}

	@Override
	public int size() { // TODO return sizeLoaded if there was error in last page request
		if ( (isFinalPage()) && (! isLoading()) ) // all loaded
			return sizeLoaded();
		
		if (totalPages >= 0) // we know number of pages
			return totalPages * AppConstants.ENTRIESPERPAGE_GALLERY;
		
		int csize = (Cache == null) ? 0 : Cache.size();
		if (sizeLoaded() > 0)
			return Math.max( sizeLoaded() + AppConstants.ENTRIESPERPAGE_GALLERY, csize);
		else {
			AsyncLoadNextPage();
			return csize;
		}
	}

	@Override
	protected void doCancel() {
		// can't do anything here as Request is not cancellable
	}

	
	
	@Override
	protected void doPageLoaded(int numItems) {
		super.doPageLoaded(numItems);
		LoadThumbnails();
	}

	@Override
	protected void doSuccessNotify(Object job) {
		// TODO queue re-update if updates was lost
		if (updateCacheTask == null) { // yes, we can miss a latest updates here but unlocking UI worth it
			updateCacheTask = new AsyncTask<Object, Integer, Integer>(){

				@Override
				protected Integer doInBackground(Object... params) {
					Thread.currentThread().setName("Save cache "+((String) params[2]));
					Utils.BrowseCache().putCache( (ViewSource) params[0], (ContentType) params[1], (String) params[2], (ArrayList<Submission>) params[3], (ArrayList<Submission>) params[4]);
					Thread.currentThread().setName("Save cache [done]");
					return null;
				}

				@Override
				protected void onPostExecute(Integer result) {
					updateCacheTask = null;
				}
				
			};
			updateCacheTask.execute(fSource, fContentType, fExtra, this, Cache);
		}
		
		super.doSuccessNotify(job);
	}

	// ====================== THUMB DOWNLOAD ======================
	private DownloadManager thumbLoader = new DownloadManager(5);
	private int thumbIndex = 0;

	/**
	 * Start or restart thumbnails loading 
	 */
	protected void LoadThumbnails() {
		Submission s = null;
		
		SharedPreferences prefs =  Utils.getPreferences();
		if (prefs != null)
			thumbLoader.setNumThreads(prefs.getInt(AppConstants.PREFERENCE_THUMB_THREADS, 5));
		
		while ( (s = get(thumbIndex, false, false)) != null) {
			if (! s.checkThumbnail())
				thumbLoader.Download(new HTTPFileDownloadTask(
						s.getThumbURL(), s.getThumbnailPath(),
						new IJobStatusCallback() {
							
							@Override
							public void onSuccess(Object job) {
								doProgressNotify(job, 0, 0, "");
							}
							
							public void onStart(Object job) {
							}
							
							public void onProgress(Object job, int progress, int total, String msg) {
							}
							
							public void onError(Object job, String msg) {
							}
						}, 
						10, false, true, null, "text", s.getThumbAttempts()));
			thumbIndex++;
		}
	}
	
	public void RefreshThumbnails() {
		thumbIndex = 0;
		LoadThumbnails();
	}
	
/*	private ThumbnailDownloader thumbLoader = null;

	protected void LoadThumbnails() {
		StopLoadThumbnails();
		
		thumbLoader = new ThumbnailDownloader() {
			@Override
			protected void onProgressUpdate(Integer... values) {
				doProgressNotify(this, values[0], sizeLoaded(), "");
			}

			@Override
			protected void onPostExecute(Integer result) {
				thumbLoader = null;
			}
			
		};
		thumbLoader.execute(this);
	}
	
	/**
	 * Stop loading thumbnails
	 */
/*	protected void StopLoadThumbnails() {
		if (thumbLoader != null)
			if (thumbLoader.cancel(false))
				thumbLoader = null;
	}
/**/
	
	@Override
	public void finalize() throws Throwable {
//		StopLoadThumbnails();
		super.finalize();
	}
	
}
