package com.sofurry.storage;

import java.util.HashMap;
import java.util.Map;

import com.sofurry.model.SubmissionList;

public class SubmissionListStorage {
	public static SubmissionListStorage singleton = null; // The hook to access this object
	
	public static SubmissionListStorage get() {
		if (singleton == null)
			singleton = new SubmissionListStorage();
		return singleton;
	}	
	
	public static Map<Long, SubmissionList> storage = new HashMap<Long, SubmissionList>();
	
	public static void store(SubmissionList list) {
		storage.put(list.getListId(), list);
	}
	
	public static SubmissionList get(long id) {
		return storage.get(id);
	}
	
	public static void remove(long id) {
		storage.remove(id);
	}
	
}
