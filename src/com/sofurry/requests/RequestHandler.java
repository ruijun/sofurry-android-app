package com.sofurry.requests;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.sofurry.RefreshRequest;

public abstract class RequestHandler implements IRequestHandler {
	
	/**
	 * Handler to handle AjaxRequestFeedback
	 */
	protected Handler requesthandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				if (msg.obj == null) {
					onError(msg.arg1,new Exception("Null return value on requestHandler"));
					return;
				}
				if (msg.obj instanceof Exception) {
					onError(msg.arg1,(Exception)msg.obj);
					return;
				}
				if (msg.obj instanceof RefreshRequest) {
					refresh();
					return;
				}
				if (msg.obj instanceof JSONObject) {
					onData(msg.arg1,(JSONObject)msg.obj);
					return;
				}
				if (msg.obj instanceof ProgressSignal) {
					onProgress(msg.arg1,(ProgressSignal)msg.obj);
					return;
				}
				onOther(msg.arg1,msg.obj);
				//onError(new Exception("Unknown return value on requestHandler ("+msg.obj.getClass().getName()+")"));
				
				
			} catch (Exception e) {
				onError(msg.arg1,e);
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
	 * @param id
	 * Ajax Request ID
	 */
	public abstract void onError(int id,Exception e);
	
	/**
	 * Method that is called when data is returned
	 * @param obj
	 * @param id
	 * Ajax Request ID
	 */
	public abstract void onData(int id,JSONObject obj);
	
	/**
	 * This is called when some process signals some progress
	 * @param id
	 * @param prg
	 */
	public abstract void onProgress(int id, ProgressSignal prg);
	
	/**
	 * Method that is called when a refresh is called
	 */
	public abstract void refresh();
	
	/**
	 * This method is called whenever none of the other events apply 
	 * @param obj
	 * The data returned via the message handler, the object is never null
	 * @param id
	 * Ajax Request ID
	 * @throws Exception
	 */
	public void onOther(int id, Object obj) throws Exception {
		throw new Exception("No handler for "+obj.getClass().getName()+" implemented.");
	}
	
	/**
	 * Posts a message to the Messagehandler
	 * @param obj
	 * The object to be passed as the message
	 * @param id
	 * The id to be passed to the callback. Usually this is the ajaxrequest's id
	 */
	public void postMessage(int id,Object obj) {
		Message msg = requesthandler.obtainMessage();
		msg.arg1 = id; // To recognize the message by
		msg.obj = obj; // The object containing data payload
		requesthandler.sendMessage(msg);
	}
	
	/**
	 * Posts a message to the Messagehandler
	 * @param obj
	 * The object to be passed as the message
	 */
	public void postMessage(Object obj) {
		postMessage(-1, obj);
	}

}
