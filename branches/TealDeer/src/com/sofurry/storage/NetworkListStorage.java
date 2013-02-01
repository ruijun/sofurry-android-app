package com.sofurry.storage;

import java.util.HashMap;
import java.util.Map;

import com.sofurry.model.NetworkList;

public class NetworkListStorage {
	public static NetworkListStorage singleton = null; // The hook to access this object
	
	public static NetworkListStorage get() {
		if (singleton == null)
			singleton = new NetworkListStorage();
		return singleton;
	}	
	
	public static Map<Long, NetworkList> storage = new HashMap<Long, NetworkList>();
	
	public static void store(NetworkList list) {
		storage.put(list.getListId(), list);
	}
	
	public static NetworkList get(long id) {
		return storage.get(id);
	}
	
	public static void remove(long id) {
		storage.remove(id);
	}
	
	public static void remove(NetworkList list) {
		if (list != null)
			storage.remove(list.getListId());
	}
}
