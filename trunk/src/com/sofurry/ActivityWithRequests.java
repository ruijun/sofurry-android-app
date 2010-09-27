package com.sofurry;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.sofurry.requests.ICanHandleFeedback;
import com.sofurry.requests.ProgressSignal;
import com.sofurry.requests.RequestHandler;
import com.sofurry.tempstorage.ItemStorage;
import com.sofurry.util.ErrorHandler;

/**
 * @author Rangarig
 *
 * The base for an Activity with request handler, and a convinient progressbar handler
 */
public abstract class ActivityWithRequests extends Activity implements ICanHandleFeedback,ICanCancel {
	
	protected ProgressBarHelper pbh = new ProgressBarHelper(this,this);
	protected long uniquestoragekey = System.nanoTime();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) { 
			uniquestoragekey = System.nanoTime();
			requesthandler = new RequestHandler(this);
		} else {
			uniquestoragekey = savedInstanceState.getLong("unique");
	    	requesthandler = (RequestHandler) retrieveObject("handler");
	    	requesthandler.setFeedbackReceive(this);
		}
		
		super.onCreate(savedInstanceState);
	}
	
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("unique", uniquestoragekey);
		storeObject("handler", requesthandler);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * The request handler to be used to handle the feedback from the AjaxRequest
	 */
	protected RequestHandler requesthandler = null;

	
	public void onError(int id, Exception e) {
		pbh.hideProgressDialog();
		ErrorHandler.showError(this, e);
	}

	public void onData(int id, JSONObject obj) throws Exception {
		pbh.hideProgressDialog();
		throw new Exception("JSONObject received, but no handler implemented.");
	}

	public void onProgress(int id, ProgressSignal prg) throws Exception {
		pbh.setProgress(prg);
	}

	public void onOther(int id, Object obj) throws Exception {
		  throw new Exception("Unexpected object type "+obj.getClass().getName()+" received.");
	}
	
	public void refresh() throws Exception {
		// Intentionally left blank
	}

	// Goes back to the story list
	protected void closeList() {
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);
		finish();
	}
	
	/**
	 * Stores an item in the global ItemStorage for later retrieval
	 * @param key
	 * The key to store the item under
	 * @param obj
	 * The object to store
	 */
	protected void storeObject(String key, Object obj) {
		ItemStorage.get().store(uniquestoragekey, key, obj);
	}
	
	/**
	 * Retrieves an object from the global ItemStroage
	 * @param key
	 * The key of the object to retrieve
	 * @return
	 * The object that was stored, or null if the object does not exist
	 */
	protected Object retrieveObject(String key) {
		return ItemStorage.get().retrieve(uniquestoragekey, key);
	}
	
	/**
	 * Clears all stored objects for this activity
	 */
	protected void clearStorage() {
		ItemStorage.get().remove(uniquestoragekey);
	}



	/* (non-Javadoc)
	 * @see com.sofurry.ICanCancel#cancel()
	 * 
	 * Is called when the progress bar is canceled
	 */
	public void cancel() {
		requesthandler.killThreads();
		finish();
	}

	
	
}
