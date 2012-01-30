package com.sofurry.requests;

/**
 * @author Rangarig
 *
 * Allows to envelop an exception, adding the ID data of the request that had sent this exception
 */
public class RequestHandlerException extends Exception {
	private static final long serialVersionUID = 1L;
	public int id = -1;
	public RequestHandlerException(int id, Exception e) {
		super(e.getMessage(),e);
		this.id = id;
	}

}
