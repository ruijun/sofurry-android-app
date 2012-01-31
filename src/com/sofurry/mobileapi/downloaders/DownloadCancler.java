package com.sofurry.mobileapi.downloaders;

/**
 * @author f034561
 *
 * A helper object that allows to cancel a download
 */
public class DownloadCancler {
	private boolean canceled = false;
	
	/**
	 * Returns true, if the operation was canceled
	 * @return
	 */
	public boolean isCanceled() {
		boolean buf = false;
		synchronized (this) {
			buf = canceled;
		}
		return buf;
	}
	
	/**
	 * Signals to the downloading thread that we would wish to cancel
	 */
	public void doCancel() {
		synchronized (this) {
			canceled = true;
		}
	}

}
