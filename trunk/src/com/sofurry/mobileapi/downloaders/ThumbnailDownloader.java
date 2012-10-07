package com.sofurry.mobileapi.downloaders;

import com.sofurry.AppConstants;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;

import android.os.AsyncTask;
import android.util.Log;

public abstract class ThumbnailDownloader extends
		AsyncTask<NetworkList<Submission>, Integer, Integer> {

	@Override
	protected Integer doInBackground(NetworkList<Submission>... arg0) {
		try {
			Log.d("[ThumbDl]", "--- Starting");

			boolean tryAgain = true;
			boolean fastmode = true;
			int itemId = 0;
			Submission s = null;
//			long threadId = System.currentTimeMillis();
			long lastRefresh = System.currentTimeMillis();
			
			while ( (! isCancelled()) && tryAgain) {
				tryAgain = false;
				itemId = 0;
				s = arg0[0].get(itemId, false);

				while ((! isCancelled()) && (s != null)) {
					if (s.getThumbAttempts() > 3) continue;  // We will give up after trying to fetch the thumbnail 3 times
					
					// The IHasThumbnail object will know what to do
					if (!s.checkThumbnail())
					  try {
						  s.populateThumbnail(fastmode);
					  } catch (Exception e) {
						  Log.d(AppConstants.TAG_STRING, "Thumbloading failed " + e.getMessage());
					  }
					
					// If fetching of one thumbnail fails, we will try the whole list again
					if (!s.checkThumbnail())
						tryAgain = true;
					
					//Don't refresh more often than once every 4 seconds
					if (System.currentTimeMillis() - lastRefresh > 4000) {
						publishProgress(itemId);
						lastRefresh = System.currentTimeMillis();
					}

					itemId++;
					s = arg0[0].get(itemId, false);
					
					if (isCancelled())
						Log.d("[ThumbDl]", "--- Cancelled");
				}
				fastmode = false;
				publishProgress(itemId);
			}
		} catch (Exception e) {
		}
		Log.d("[ThumbDl]", "--- Finish");
		return 0;
	}

}
