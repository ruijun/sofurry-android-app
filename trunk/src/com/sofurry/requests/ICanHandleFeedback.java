package com.sofurry.requests;

import org.json.JSONObject;

import com.sofurry.util.ProgressSignal;

public interface ICanHandleFeedback {

	/**
	 * Method that is called when an error occurs
	 * @param e
	 * @param id
	 * Ajax Request ID
	 */
	public abstract void onError(Exception e);

	/**
	 * Method that is called when data is returned
	 * @param obj
	 * @param id
	 * Ajax Request ID
	 */
	public abstract void onData(int id, JSONObject obj) throws Exception;

	/**
	 * This is called when some process signals some progress
	 * @param id
	 * @param prg
	 */
	public abstract void onProgress(int id, ProgressSignal prg) throws Exception;

	/**
	 * Method that is called when a refresh is called
	 */
	public abstract void refresh() throws Exception;

	/**
	 * This method is called whenever none of the other events apply 
	 * @param obj
	 * The data returned via the message handler, the object is never null
	 * @param id
	 * Ajax Request ID
	 * @throws Exception
	 */
	public abstract void onOther(int id, Object obj) throws Exception;

}