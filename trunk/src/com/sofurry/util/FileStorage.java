package com.sofurry.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;

public class FileStorage {

	private static boolean mExternalStorageAvailable = false;
	private static boolean mExternalStorageWriteable = false;

	public static FileOutputStream getFileOutputStream(String filename) throws IOException {
		checkExternalMedia();
		if (!mExternalStorageWriteable)
			return null;
		
		File d = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.sofurry/files/");
		d.mkdirs();
				
		File f = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.sofurry/files/"+filename);
		if (f.createNewFile() && f.canWrite()) {
			return new FileOutputStream(f);
		}
		return null;
	}
	
	public static FileInputStream getFileInputStream(String filename) throws FileNotFoundException {
		checkExternalMedia();
		if (!mExternalStorageAvailable)
			return null;
		
		File f = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.sofurry/files/"+filename);
		if (f.canRead()) {
			return new FileInputStream(f);
		}
		return null;
	}
		
	
	private static void checkExternalMedia() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}
}
