package com.sofurry.requests;

import org.json.JSONObject;

import android.os.Message;

import com.sofurry.mobileapi.core.CallBack;
import com.sofurry.mobileapi.downloaders.AsyncFileDownloader;
import com.sofurry.mobileapi.downloaders.PercentageFeedback;

/**
 * @author Rangarig
 * 
 * A convinience class to wrap an async download from the SF Mobile api,
 * and handle the callbacks via the message handler.
 * 
 * You need to provide classes to setPercentageFeedback and setFinishFeedback
 * if you want a progress bar, and if you want to know when the download has
 * finished.
 * 
 * All calls will be distributed though the messagehandler, so they are guisafe.
 *
 */
public class AndroidDownloadWrapper {
	public RequestHandler req = null;
	
	private String url = null;
	private String targetFile = null;
	
	public AsyncFileDownloader downloader = null;
	public DataCall callPercent = null;
	public DataCall callFinish = null;
	
	public AndroidDownloadWrapper(RequestHandler reqHandler, String url, String targetFile) {
		this.url = url;
		this.targetFile = targetFile;
		this.req = reqHandler;
	}
	
	/**
	 * Sets the Annonymous class to be called when percentage feedback is available
	 * @param dc
	 */
	public void setPercentageFeedback(DataCall dc) {
		callPercent = dc;
	}
	
	public void setFinishFeedback(DataCall dc) {
		callFinish = dc;
	}
	
	/**
	 * Starts the asyncronous download. The method will return immediately.
	 * Feedback will be sent according to the objects passed to setFinishFeedback
	 * and setPercentageFeedback.
	 */
	public void start() {
		try {
			
			downloader = new AsyncFileDownloader(url, targetFile,
			  // Implement callback for error and success
			  new CallBack(){ 
				public void success(JSONObject result){
					relayData(result);
				}; 
				public void fail(Exception e){
					Message msg = new Message();
					msg.obj = new DataCall(e) {
						public void call() {
							req.onError((Exception)arg1);
						}
					};
					req.postMessage(msg);
				}; 
			  }
			,
			// Implement the callback for percentage feedback
			new PercentageFeedback() {
				public void signalPercentage(int prog, int goal) {
					relayFeedback(prog, goal);
				}
			}
			);
			downloader.start();
		} catch (Exception e) {
			req.onError(e);
		}
	}
	
	/**
	 * Will try its best to stop the download currently in progress
	 */
	public void cancel() throws Exception {
		if (downloader != null)
		  downloader.cancel();
	}
	
	/**
	 * Makes the message handler call the specified method
	 * @param result
	 */
	public void relayData(JSONObject result) {
		if (callFinish == null) return; // Nothing to do
		Message msg = new Message();
		callFinish.arg1 = result;
		msg.obj = callFinish;
		req.postMessage(msg);
	}
	
	/**
	 * Makes the message handler call for the percentage feedback
	 * @param pos
	 * The signaled position
	 * @param goal
	 * the goal
	 */
	public void relayFeedback(int pos, int goal) {
		Message msg = new Message();
		callPercent.arg1 = pos;
		callPercent.arg2 = goal;
		msg.obj = callPercent;
		req.postMessage(msg);
		
	}

}
