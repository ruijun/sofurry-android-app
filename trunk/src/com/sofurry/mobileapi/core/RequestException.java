package com.sofurry.mobileapi.core;

/**
 * @author Rangarig
 *
 * An Exception that encapsulates the request that created the exception, so that the
 * Exceptionhandler can determine the context of the exception
 */
public class RequestException extends Exception {

	private Request sourceRequest = null; // The request that caused the exception
	private static final long serialVersionUID = -6970463453884336542L;
	
	
	/**
	 * Returns the Request that caused the Exception
	 * @return
	 * A Request object that caused an exception
	 */
	public Request getSourceRequest() {
		return sourceRequest;
	}
	
	public RequestException(Exception e, Request req) {
		super(e.getMessage(),e);
		sourceRequest = req;
	}

}
