package com.sofurry.base.interfaces;

/**
 * Asynchronous job status callback
 * Well... It's extended implementation of CallBack interface. 
 * Original CallBack interface leaved intact to keep old code working  
 * @author Night_Gryphon
 *
 */
public interface IJobStatusCallback {
	public abstract void onStart(Object job);
	public abstract void onSuccess(Object job);
	public abstract void onError(Object job, String msg);
	public abstract void onProgress(Object job, int progress, int total, String msg);
}
