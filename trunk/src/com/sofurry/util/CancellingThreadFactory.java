package com.sofurry.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.sofurry.base.interfaces.ICanCancel;

/**
 * Create threads that call runnables cancel() method when interrupted()
 * @author user
 */
public class CancellingThreadFactory implements ThreadFactory {
	public class CancellingThread extends Thread {
		private ICanCancel canceller = null;

		public CancellingThread(ThreadGroup group, Runnable runnable,
				String threadName, long stackSize) {
			super(group, runnable, threadName, stackSize);
			if (runnable instanceof ICanCancel)
				canceller = (ICanCancel) runnable;
		}

		@Override
		public void interrupt() {
			if (canceller != null)
				canceller.cancel();
			super.interrupt();
		}
	}

	// ================================================================
    static final AtomicInteger poolNumber = new AtomicInteger(1);
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;

    public CancellingThreadFactory() {
		   SecurityManager s = System.getSecurityManager();
	        group = (s != null)? s.getThreadGroup() :
	                             Thread.currentThread().getThreadGroup();
	        namePrefix = "p" + 
	                      poolNumber.getAndIncrement() + 
	                     "-t";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new CancellingThread(group, r, 
                namePrefix + threadNumber.getAndIncrement(),
                0);
		if (t.isDaemon())
			t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}

}
