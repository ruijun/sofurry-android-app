package com.sofurry.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

/**
 * @author SoFurry
 * 
 * Storage facility for images
 */
public class ImageStorage {

	
	public static Bitmap loadSubmissionImage(int id) {
		return loadIcon("image"+id);
	}

	public static void saveSubmissionImage(int id, Bitmap icon) {
		saveIcon("image"+id, icon);
	}
	
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
		FileInputStream is = null;
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
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return bitmap;
	}
	
	private static void saveIcon(String filename, Bitmap icon) {
		FileOutputStream os = null;
		try {
			os = FileStorage.getFileOutputStream(filename );
			if (os != null) {
				icon.compress(CompressFormat.JPEG, 80, os);
			} else {
				Log.w("soFurryApp", "Can't save to external storage");
			}
		} catch (Exception e) {
			Log.e("soFurryApp", "error in saveIcon", e);
		} finally {
			if (os != null) {
				try {
					os.flush();
					os.close();
				} catch (Exception e2) {
					// Intentionally left blank
				}
			}
		}
		Log.d("soFurryApp", "icon saved " + filename);
	}
	
}
