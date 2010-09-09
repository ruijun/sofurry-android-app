package com.sofurry.requests;

import org.json.JSONObject;

import com.sofurry.RefreshRequest;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public abstract class RequestHandler implements IRequestHandler {
	
	/**
	 * Handler to handle AjaxRequestFeedback
	 */
	protected Handler requesthandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				if (msg.obj instanceof Exception) {
					onError((Exception)msg.obj);
					return;
				}
				if (msg.obj instanceof JSONObject) {
					onData((JSONObject)msg.obj);
					return;
				}
				if (msg.obj instanceof Bitmap) {
					onBitmap((Bitmap)msg.obj);
					return;
				}
				if (msg.obj instanceof RefreshRequest) {
					refresh();
					return;
				}
				if (msg.obj == null) {
					onError(new Exception("Null return value on requestHandler"));
					return;
				}
				onError(new Exception("Unknown return value on requestHandler ("+msg.obj.getClass().getName()+")"));
				
				
			} catch (Exception e) {
				onError(e);
			}
		}
	};
	
	/* (non-Javadoc)
	 * @see com.sofurry.requests.IRequestHandler#getRequestHandler()
	 * Returns the request handler for this session
	 */
	public Handler getRequestHandler() {
		return requesthandler;
	}
	
	/**
	 * Method that is called when an error occurs
	 * @param e
	 */
	public abstract void onError(Exception e);
	
	/**
	 * Method that is called when data is returned
	 * @param obj
	 */
	public abstract void onData(JSONObject obj);
	
	/**
	 * Method that is called when a refresh is called
	 */
	public abstract void refresh();
	
	public void onBitmap(Bitmap bmp) throws Exception {
		throw new Exception("No handler for bitmap implemented.");
	}
	
	/**
	 * Posts a message to the Messagehandler
	 * @param obj
	 * The object to be passed as the message
	 */
	public void postMessage(Object obj) {
		Message msg = requesthandler.obtainMessage();
		msg.obj = obj;
		requesthandler.sendMessage(msg);
	}
	

}
