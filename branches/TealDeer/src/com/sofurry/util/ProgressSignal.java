package com.sofurry.util;

/**
 * @author Rangarig
 * 
 * Simple Wrapper class, used to signal the Progress of a long lasting operation.
 * 
 * Usually percentage values will be passed
 *
 */
@Deprecated
public class ProgressSignal {
	public int progress = 0; // The current progress value
	public int goal = 0;     // The maximum reachable value
	
	/**
	 * Creates progress values
	 * @param prog
	 * @param goal
	 */
	public ProgressSignal(int prog, int goal) {
		this.progress = prog;
		this.goal = goal;
	}
	
	/**
	 * Returns the current progress as Percantage value
	 * @return
	 */
	public int getPercent() {
		return (int)((100.0 / goal) * progress);
	}

}
