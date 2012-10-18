package com.sofurry.mobileapi.downloadmanager;

import android.os.Handler;

import com.sofurry.base.interfaces.ICanCancel;
import com.sofurry.base.interfaces.IJobStatusCallback;


/**
 * Represents abstract downloader task 
 * @author Night_Gryphon
 */
public abstract class abstractDownloadTask implements Runnable, IJobStatusCallback, ICanCancel {
	private IJobStatusCallback fCallback = null;
	
	private int fRetryLeft = 3;
	protected boolean isCancelled = false;
	private Handler handler = new Handler(); // handler in creator thread to post callback to
	
	public abstractDownloadTask(IJobStatusCallback aCallback, int aRetryCount) {
		super();
		this.fCallback = aCallback;
		this.fRetryLeft = aRetryCount;
	}
	
	@Override
	public void run() {
		final String orgName = Thread.currentThread().getName();
        Thread.currentThread().setName("DL "+orgName+" "+getThreadName());

		onStart(this);
		do {
			fRetryLeft--;
			try {
				doDownload();
				onSuccess(this);
				break;
			} catch (Exception e) {
				onError(this, e.getMessage());
			}
		} while ((fRetryLeft > 0) && (! isCancelled));
		
        Thread.currentThread().setName(orgName);
	}

	protected abstract void doDownload() throws Exception;
	
	public abstract String getThreadName();
	
	@Override
	public void onStart(Object job) {
		if (fCallback != null)
			handler.post(new Runnable() {
				@Override
				public void run() {
					fCallback.onStart(abstractDownloadTask.this);
				}
			});
	}

	@Override
	public void onSuccess(Object job) {
		if (fCallback != null)
			handler.post(new Runnable() {
				@Override
				public void run() {
					fCallback.onSuccess(abstractDownloadTask.this);
				}
			});
	}

	@Override
	public void onError(Object job, final String msg) {
		if (fCallback != null)
			handler.post(new Runnable() {
				@Override
				public void run() {
					fCallback.onError(abstractDownloadTask.this, msg);
				}
			});
	}

	@Override
	public void onProgress(Object job, final int progress, final int total, final String msg) {
		if (fCallback != null)
			handler.post(new Runnable() {
				@Override
				public void run() {
					fCallback.onProgress(abstractDownloadTask.this, progress, total, msg);
				}
			});
	}
	
	public int getRetryLeft() {
		return fRetryLeft;
	}
	
	public void cancel() {
		isCancelled = true;
	}
}
