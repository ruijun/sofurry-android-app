package com.sofurry.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sofurry.AppConstants;

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

	
	public static String SUBMISSION_IMAGE_PATH = "image";
	public static String THUMB_PATH = "thumb";
	public static String AVATAR_PATH = "avatar";
	
	/**
	 * Loads a submission image from storage
	 * @param filename
	 * The filename to load
	 * @return
	 */
	public static Bitmap loadSubmissionImage(String filename) {
		return loadBitmap(getSubmissionImagePath(filename));
	}
	
	/**
	 * Returns the submission image path, minus the path to the apps file storage.
	 * Needs to be encapsulated in FileStorage.getPath(getSubmissionImagePath(id))
	 * @param id
	 * The id of the image to show.
	 * @return
	 */
	public static String getSubmissionImagePath(String filename) {
		return SUBMISSION_IMAGE_PATH + "/" + filename;
	}

//	public static void saveSubmissionImage(int id, Bitmap icon) throws Exception  {
//		saveBitmap(getSubmissionImagePath(id), icon);
//	}
	
	public static Bitmap loadSubmissionIcon(int id) {
		return loadBitmap(THUMB_PATH +"/t"+id);
	}

	public static void saveSubmissionIcon(int id, Bitmap icon) throws Exception  {
		saveBitmap(THUMB_PATH +"/t"+id, icon);
	}

	public static Bitmap loadUserIcon(int uid) {
		return loadBitmap(AVATAR_PATH + "/a"+uid);
	}

	public static void saveUserIcon(int uid, Bitmap icon) throws Exception {
		saveBitmap(AVATAR_PATH + "/a"+uid, icon);
	}
	
	
	/**
	 * Attempts to load an icon from the specified place
	 * @param filename
	 * The filename of the icon e.g. avatars/a121212
	 * @return
	 */
	private static Bitmap loadBitmap(String filename) {
		FileInputStream is = null;
		Bitmap bitmap = null;
		try {
			is = FileStorage.getFileInputStream(filename);
			if (is != null && is.available() > 0) {
				bitmap = BitmapFactory.decodeStream(is);
			} else {
				Log.w(AppConstants.TAG_STRING, "ImageStorage: Can't load from external storage");
			}
		} catch (Exception e) {
			Log.e(AppConstants.TAG_STRING, "ImageStorage: Error in loadIcon", e);
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
	
	/**
	 * Saves an icon into the specified path
	 * @param filename
	 * file name e.G. images/somedragon.jpg
	 * @param icon
	 * @throws Exception
	 */
	private static void saveBitmap(String filename, Bitmap icon) throws Exception{
		if (icon == null) throw new Exception("Attempt to store null icon for " + filename);
		FileOutputStream os = null;
		try {
			os = FileStorage.getFileOutputStream(filename );
			if (os != null) {
				icon.compress(CompressFormat.JPEG, 80, os);
			} else {
				throw new Exception("GetFileOutputstream for filename failed.");
			}
		} catch (Exception e) {
			throw new Exception("Saving Icon failed.",e );
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
		Log.d(AppConstants.TAG_STRING, "ImageStorage: Icon saved " + filename);
	}
	
	/**
	 * Löscht alle in dem Ordner enthaltenen Dateien
	 * @throws Exception
	 */
	public static void cleanupImages() throws Exception {
		FileStorage.cleanup(SUBMISSION_IMAGE_PATH + "/");
	}
	
	
}
