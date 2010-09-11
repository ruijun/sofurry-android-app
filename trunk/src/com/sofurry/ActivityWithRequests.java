package com.sofurry;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sofurry.requests.RequestHandler;
import com.sofurry.util.ErrorHandler;

/**
 * @author Rangarig
 *
 * The base for an Activity with request handler, and a convinient progressbar handler
 */
public abstract class ActivityWithRequests extends Activity {
	
	protected ProgressBarHelper pbh = new ProgressBarHelper(this);
	
	/**
	 * The request handler to be used to handle the feedback from the AjaxRequest
	 */
	protected RequestHandler requesthandler = new RequestHandler() {
		
		@Override
		public void onError(int id,Exception e) {
			pbh.hideProgressDialog();
			sonError(id,e);
		}
		
		@Override
		public void onData(int id, JSONObject obj) {
			pbh.hideProgressDialog();
			try {
				sonData(id,obj);
			} catch (Exception e) {
				sonError(id, e);
			}
		}
		
		@Override
		public void refresh() {
			refresh();
		}

		@Override
		public void onOther(int id,Object obj) throws Exception {
			try {
				sonOther(id,obj);
			} catch (Exception e) {
				sonError(id, e);
			}
		}
	};
	
	public void sonError(int id,Exception e) {
		ErrorHandler.showError(this, e);
	}
	
	public void sonData(int id,JSONObject obj) throws Exception{
		throw new Exception("JSONObject received, but no handler implemented.");
	}

	public void refresh() {
	}
	
	public void sonOther(int id, Object obj) throws Exception {
	  throw new Exception("Unexpected object type "+obj.getClass().getName()+" received.");
	}
	
	// Goes back to the story list
	protected void closeList() {
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);
		finish();
	}


}
