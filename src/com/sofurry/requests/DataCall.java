package com.sofurry.requests;

/**
 * @author Rangarig
 * Used to call Methods from the MessageHandler, so Workerthreads do not interfere with gui affairs.
 * 
 * Arg1 and Arg2 can be used by the anonymous method
 */
public abstract class DataCall {
	// Constructor for the DataCall object
	public DataCall(Object arg1) {
		this.arg1 = arg1;
	}

	// Constructor for the DataCall object, with two parameters
	public DataCall(Object arg1, Object arg2) {
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	// To be used when no parameters need to be passed
	public DataCall() {
	}

	// usable argument 1
	public Object arg1 = null;
	// usable argument 2
	public Object arg2 = null;
	/**
	 * The method called by the request handler
	 */
	public abstract void call() throws Exception;
}
