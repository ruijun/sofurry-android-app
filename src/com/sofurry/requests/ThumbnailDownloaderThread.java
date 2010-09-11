package com.sofurry.requests;

import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

import com.sofurry.RefreshRequest;
import com.sofurry.model.IHasThumbnail;

/**
 * @author SoFurry
 *
 * Downloads thumbnails for a Gallery or Listview
 */
public class ThumbnailDownloaderThread extends Thread {
	
	private boolean runIt = true;
	//private boolean saveUserAvatar = false;
	private IRequestHandler updateHandler;
	private ArrayList<IHasThumbnail> resultList;

	/**
	 * Creates a Thumbnail Thread, that will download thumbnails in the background and refresh your view occasionally
	 * @param mode
	 * UserIcon: Stores the downloaded ICON in the icon storage
	 * SubmissionIcon: Stores the downloaded icon in the userIcon storage
	 * @param handler
	 * The result handler that will handle the refreshing
	 * @param resultList
	 * The list of submissions to download icons to
	 */
	public ThumbnailDownloaderThread(IRequestHandler handler, ArrayList<IHasThumbnail> resultList) {
		this.updateHandler = handler;
		this.resultList = resultList;
	}

	/**
	 * Called to terminate the thread neatly
	 */
	public void stopThread() {
		runIt = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 * The main thread, that runs, and downloads bitmaps if it can.
	 */
	public void run() {
		try {
			Iterator<IHasThumbnail> i = resultList.iterator();
			long lastRefresh = 0;
			while (runIt && i.hasNext()) {
				IHasThumbnail s = i.next();
				if (s == null) continue;
				
				// The IHasThumbnail object will know what to do
				if (s.getThumbnail() == null)
				  s.populateThumbnail();
				
				//Don't refresh more often than once every 4 seconds
				if (System.currentTimeMillis() - lastRefresh > 4000) {
					triggerRefresh();
					lastRefresh = System.currentTimeMillis();
				}
			}
			triggerRefresh();
		} catch (Exception e) {
			updateHandler.postMessage(e);
		}
	}

	/**
	 * Triggers a refresh in the view this downloader is attached to
	 */
	private void triggerRefresh() {
		Log.i("SF ThumbDownloader", "Updating listview");
		updateHandler.postMessage(new RefreshRequest());
	}
}
