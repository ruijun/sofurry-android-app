package com.sofurry;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sofurry.model.Submission;
import com.sofurry.util.ContentDownloader;
import com.sofurry.util.IconStorage;

// TODO: This only works for art/stories/music/journals right now, NOT PMs
public class ThumbnailDownloaderThread extends Thread {
	boolean runIt = true;
	boolean saveUserAvatar = false;
	Handler updateHandler;
	ArrayList<Submission> resultList;
	
	// Set saveUserAvatar to true to save the returned thumbnail as the submission's user avatar
	public ThumbnailDownloaderThread(boolean saveUserAvatar, Handler updateHandler, ArrayList<Submission> resultList) {
		this.saveUserAvatar = saveUserAvatar;
		this.updateHandler = updateHandler;
		this.resultList = resultList;
	}

	public void stopThread() {
		runIt = false;
	}

	public void run() {
		Iterator<Submission> i = resultList.iterator();
		while (runIt && i.hasNext()) {
			Submission s = i.next();
			if (s != null && s.getId()!=-1 && s.getThumbnail() == null) {
				Log.i("SF ThumbDownloader", "Downloading thumb for pid " + s.getId() + " from "
						+ s.getThumbnailUrl());
				Bitmap thumbnail = ContentDownloader.downloadBitmap(s.getThumbnailUrl());
				s.setThumbnail(thumbnail);
				Log.i("SF ThumbDownloader", "Storing image");
				if (saveUserAvatar)
					IconStorage.saveUserIcon(Integer.parseInt(s.getAuthorID()), thumbnail);
				else
					IconStorage.saveSubmissionIcon(s.getId(), thumbnail);

				Log.i("SF ThumbDownloader", "Updating listview");
		        Message msg = updateHandler.obtainMessage();
		        msg.obj = null;
		        updateHandler.sendMessage(msg);			}
		}
	}
}
