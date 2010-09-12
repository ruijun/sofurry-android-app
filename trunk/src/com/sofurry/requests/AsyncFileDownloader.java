package com.sofurry.requests;

import com.sofurry.util.ContentDownloader;

/**
 * @author Rangarig
 *
 * A class to be used to download a file asyncroniously
 */
public class AsyncFileDownloader extends Thread {

	private IRequestHandler req = null;
	private String url = null;
	private String filename = null;
	private int id = 0; // The ID to recognize the file feedback by
	
	/**
	 * Downloads a file, and signals completion to the passed request handler
	 * @param req
	 * The requestHandler to pass the complete signal, to. (AsyncFileDownloader object will be returned)
	 * @param url
	 * The url to fetch from
	 * @param filename
	 * The filename to write to
	 * @param id
	 * The ID to be passed along with the object
	 * @return
	 */
	public static AsyncFileDownloader doRequest(IRequestHandler req, String url, String filename, int id) {
	  AsyncFileDownloader dl = new AsyncFileDownloader(req, url, filename, id);
	  dl.start();
	  return dl;
	}
	
	public AsyncFileDownloader(IRequestHandler req, String url,	String filename, int id) {
		super();
		this.req = req;
		this.url = url;
		this.filename = filename;
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			ContentDownloader.downloadFile(url, filename);
			req.postMessage(id, this);
		} catch (Exception e) {
			req.postMessage(id, e);
		}
	}
	
	
	

}
