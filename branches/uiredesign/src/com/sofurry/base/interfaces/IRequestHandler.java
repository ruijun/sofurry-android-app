package com.sofurry.base.interfaces;

import android.os.Handler;

/**
 * @author rangarig
 * 
 * An interface that must be implemented if the RequestThread is to be used
 *
 */
public interface IRequestHandler {
	public abstract Handler getRequestHandler();
	public void postMessage(int id, Object obj);	
	public void postMessage(Object obj);
	public void postMessageInline(int id, Object obj);
	public boolean isKilled(); // Returns true if the thread this request handler belongs to, is to terminate
}
