/**
 * 
 */
package com.sofurry.storage;

import java.util.HashMap;

/**
 * @author Rangarig
 * 
 * A storage place to store items, for the onSuspend and onResume services.
 * Probably not the proper way to do things, should work though.
 *
 */
public class ItemStorage {
	
	public static ItemStorage singleton = null; // The hook to access this object
	
	public HashMap<Long, HashMap<String, Object>> storage = new HashMap<Long, HashMap<String,Object>>();
	
	/**
	 * Returns the single instance of the item storage
	 * @return
	 */
	public static ItemStorage get() {
		if (singleton == null)
			singleton = new ItemStorage();
		return singleton;
	}
	
	/**
	 * Stores data in storage
	 * @param key
	 * The activities unique key
	 * @param subkey
	 * The key to store the object under
	 * @param data
	 * The object
	 */
	public void store(long key, String subkey, Object data) {
		// Does storage slot exist?
		HashMap<String, Object> folder = storage.get(key);
		if (folder == null) {
			folder = new HashMap<String, Object>();
			storage.put(key, folder);
		}
		if (data != null)
		  folder.put(subkey, data);
		else
		  folder.remove(subkey);
	}
	
	/**
	 * Returns an object from storage
	 * @param key
	 * The Activitys unique key
	 * @param subkey
	 * The subkey, or the object to be returned
	 * @return
	 */
	public Object retrieve(long key, String subkey) {
		HashMap<String, Object> folder = storage.get(key);
		if (folder == null) return null;
		
		return folder.get(subkey);
	}
	
	/**
	 * Removes the whole storage object from storage
	 * @param key
	 * The key to remove
	 * @return
	 * Returns nothing
	 */
	public void remove(long key) {
		storage.remove(key);
	}

}
