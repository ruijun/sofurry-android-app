package com.sofurry.requests;

import org.json.JSONObject;

import android.os.Message;

import com.sofurry.mobileapi.core.CallBack;
import com.sofurry.mobileapi.core.Request;

/**
 * @author Rangarig
 * 
 * Convinience Class to use sofurry mobile api request objects in an android enviroment
 * Errors will automatically be forwarded to the request handler, and the request handler method
 * call is all that has to be passed.
 *
 */
public class AndroidRequestWrapper {
	public RequestHandler req = null;
	public Request toWrap = null;
	public DataCall toCall = null;
	
	public AndroidRequestWrapper(RequestHandler reqHandler, Request request) {
		this.req = reqHandler;
		this.toWrap = request;
	}
	
	/**
	 * Does an asyncronous request, and will then execute the passed anonymous class
	 * on success. Errors will automatically be forwarded to the requestHandlers on error
	 * method
	 * @param toCall
	 * An anonymous class like this:
	 * new DataCall() {
	 *		public void call() {
	 *			handleMyData((Exception)arg1);
	 *		}
	 *	};
	 * The arg1 parameter of the anonymous class will be filled with the data object
	 * on return.
	 * 
	 * @throws Exception
	 */
	public void exec(DataCall toCall) {
		try {
			this.toCall = toCall;
			toWrap.executeAsync(
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
			);
		} catch (Exception e) {
			req.onError(e);
		}
	}
	
	/**
	 * Makes the message handler call the specified method
	 * @param result
	 */
	public void relayData(JSONObject result) {
		Message msg = new Message();
		toCall.arg1 = result;
		msg.obj = toCall;
		req.postMessage(msg);
	}
	
	
	
}
