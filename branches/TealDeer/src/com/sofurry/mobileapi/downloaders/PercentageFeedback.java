package com.sofurry.mobileapi.downloaders;

/**
 * @author Rangarig
 * 
 * a class to handle Percentage feedback messages
 *
 */
public abstract class PercentageFeedback {
	public abstract void signalPercentage(int prog, int goal);
}
