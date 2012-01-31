package com.sofurry.mobileapi.downloaders;

import org.json.JSONObject;

import com.sofurry.mobileapi.core.CallBack;

/**
 * @author Rangarig
 *
 * A class to be used to download a file asyncroniously
 */
public class AsyncFileDownloader extends Thread {

	private String url = null;
	private String filename = null;
	private CallBack callback = null;
	private PercentageFeedback feed = null;
	private DownloadCancler downcancel = null;
	//private int id = 0; // The ID to recognize the file feedback by
	
	/**
	 * Creates an Asyncronous File Downloader that will download a file in the background
	 * @param url
	 * The URL to download the file from
	 * @param filename
	 * The filename to store the file into
	 * @param cb
	 * The callback object to call once the transfer is finished
	 * @param feed
	 * The feedback object to signal feedback to
	 */
	public AsyncFileDownloader(String url, String filename, CallBack cb, PercentageFeedback feed) {
		super();
		this.callback = cb;
		this.url = url;
		this.filename = filename;
		this.feed = feed;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			downcancel = new DownloadCancler();
			ContentDownloader.downloadFile(url, filename, feed, downcancel);
			if (!downcancel.isCanceled()) // Only call the success callback routine if the download was not canceled
			  callback.success(new JSONObject());
			//req.postMessage(id, this);
		} catch (Exception e) {
			callback.fail(e);
		}
	}
	
	/**
	 * cancels the download
	 */
	public void cancel() {
		downcancel.doCancel();
	}
	
	
	

}
