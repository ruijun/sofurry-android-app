package com.sofurry.mobileapi.downloadmanager;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Manage list of downloads. Support multithread downloading;
 * @author Night_Gryphon
 */
public class DownloadManager {
	private final ThreadPoolExecutor pool;

	public DownloadManager(int numThreads) {
		super();
		pool = new ThreadPoolExecutor(numThreads, numThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
	}
	
	public void setNumThreads(int numThreads) {
		if (pool instanceof ThreadPoolExecutor) {
			((ThreadPoolExecutor) pool).setMaximumPoolSize(numThreads);
			((ThreadPoolExecutor) pool).setCorePoolSize(numThreads);
		}
	}
	
	public void Download(abstractDownloadTask dlTask) {
		if (dlTask != null)
			pool.submit(dlTask);
	}

	@Override
	protected void finalize() throws Throwable {
		pool.shutdown(); // shutdown idle threads/tasks
		pool.shutdownNow(); // Cancel currently executing tasks
	    // Wait a while for tasks to respond to being cancelled
	    if (!pool.awaitTermination(10, TimeUnit.SECONDS))
	       System.err.println("Some downloaders did not terminate");
	       
		super.finalize();
	}
	
	public long getSize() {
		return pool.getTaskCount();
	}
	
}
