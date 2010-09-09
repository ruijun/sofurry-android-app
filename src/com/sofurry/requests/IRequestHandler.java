package com.sofurry.requests;

import android.os.Handler;

/**
 * @author rangarig
 * 
 * An interface that must be implemented if the RequestThread is to be used
 *
 */
public interface IRequestHandler {
	public abstract Handler getRequestHandler();
}
