package com.sofurry.tempstorage;

import java.util.HashMap;
import java.util.Map;

import com.sofurry.base.classes.ActivityManager;
import com.sofurry.base.interfaces.IManagedActivity;

/**
 * @author Rangarig
 *
 * Storage Facitility for Activity Managers. When orientation changes occur, this is used to store the management
 * object, so it can be reused by the re-instanced activity.
 */
public class ManagerStore {
	public static Map<String, ActivityManager> storage = new HashMap<String, ActivityManager>();
	
	/**
	 * Stores an ActivityManager for reuse after orientation change
	 * @param act
	 */
	public static void store(IManagedActivity act) {
		ActivityManager man = act.getActivityManager();
		String key = "" + act.getUniqueKey();
		
		storage.put(key, man);
	}
	
	/**
	 * Returns true if a stored activity manager exists
	 * @param act
	 * @return
	 */
	public static boolean isStored(IManagedActivity act) {
		String key = "" + act.getUniqueKey();
		ActivityManager man = storage.get(key);
		if (man != null) return true;
		return false;
	}
	
	/**
	 * Retrieves the Activity manager from the storage
	 * @param act
	 * @return
	 */
	public static ActivityManager retrieve(IManagedActivity act) {
		String key = "" + act.getUniqueKey();
		ActivityManager man = storage.get(key);
		if (man != null)
		  man.setActivity(act); // Important! The activity object at this point is likely brand new!
		storage.remove(key);
		return man;
	}
	
}
