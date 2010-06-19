package com.sofurry.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class IconStorage {

	
	public static Bitmap loadSubmissionIcon(int id) {
		return loadIcon("thumb"+id);
	}

	public static void saveSubmissionIcon(int id, Bitmap icon) {
		saveIcon("thumb"+id, icon);
	}

	public static Bitmap loadUserIcon(int uid) {
		return loadIcon("avatar"+uid);
	}

	public static void saveUserIcon(int uid, Bitmap icon) {
		saveIcon("avatar"+uid, icon);
	}
	
	
	private static Bitmap loadIcon(String filename) {
		FileInputStream is;
		Bitmap bitmap = null;
		try {
			is = FileStorage.getFileInputStream(filename);
			if (is != null && is.available() > 0) {
				bitmap = BitmapFactory.decodeStream(is);
			} else {
				Log.w("soFurryApp", "Can't load from external storage");
			}
		} catch (Exception e) {
			Log.e("soFurryApp", "error in loadIcon", e);
		}
		
		return bitmap;
	}
	
	private static void saveIcon(String filename, Bitmap icon) {
		FileOutputStream os;
		try {
			os = FileStorage.getFileOutputStream(filename );
			if (os != null) {
				icon.compress(CompressFormat.JPEG, 80, os);
			} else {
				Log.w("soFurryApp", "Can't save to external storage");
			}
		} catch (Exception e) {
			Log.e("soFurryApp", "error in saveIcon", e);
		}
	}
	
}
