package com.sofurry.mobileapi.core;

public class RequestParsingException extends Exception {

	public RequestParsingException() {
	}

	public RequestParsingException(String detailMessage) {
		super(detailMessage);
	}

	public RequestParsingException(Throwable throwable) {
		super(throwable);
	}

	public RequestParsingException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
