package com.sofurry.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class IconStorage {

	
	public Bitmap loadSubmissionIcon(int id) {
		return loadIcon("thumb"+id);
	}

	public void saveSubmissionIcon(int id, Bitmap icon) {
		saveIcon("thumb"+id, icon);
	}

	public Bitmap loadUserIcon(int uid) {
		return loadIcon("avatar"+uid);
	}

	public void saveUserIcon(int uid, Bitmap icon) {
		saveIcon("avatar"+uid, icon);
	}
	
	
	private static Bitmap loadIcon(String filename) {
		FileInputStream is;
		Bitmap bitmap = null;
		try {
			is = FileStorage.getFileInputStream(filename);
			if (is != null && is.available() > 0) {
				bitmap = BitmapFactory.decodeStream(is);
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
			}
		} catch (Exception e) {
			Log.e("soFurryApp", "error in saveIcon", e);
		}
	}
	
}
