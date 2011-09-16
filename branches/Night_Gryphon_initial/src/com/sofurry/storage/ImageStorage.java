package com.sofurry.storage;

import java.io.File;
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
	
	// ============== Submission ===================
	/**
	 * Returns complete submission image path in cache
	 * @param filename
	 * The filename of the image to show.
	 * @return
	 */
	public static String getSubmissionImagePath2(String filename) {
		return FileStorage.getPath2(SUBMISSION_IMAGE_PATH + "/" + filename);
	}

	public static Boolean checkSubmissionImage(String filename) {
		return FileStorage.fileExists2(getSubmissionImagePath2(filename));
	}
	
	/**
	 * Loads a submission image from storage
	 * @param filename
	 * The relative filename to load
	 * @return
	 */
	public static Bitmap loadSubmissionImage(String relative_filename, int MaxSize) {
		return loadBitmap2(getSubmissionImagePath2(relative_filename), MaxSize);
	}
	
//	public static void saveSubmissionImage(int id, Bitmap icon) throws Exception  {
//		saveBitmap(getSubmissionImagePath(id), icon);
//	}
	
	// ================== SubmissionIcon =====================
	public static String getSubmissionIconPath2(int id) {
		return FileStorage.getPath2(THUMB_PATH +"/t"+id);
	}

	public static Boolean checkSubmissionIcon(int id) {
		return FileStorage.fileExists2(getSubmissionIconPath2(id));
	}

	public static Bitmap loadSubmissionIcon(int id) {
		return loadBitmap2(getSubmissionIconPath2(id), 0);
	}

	public static void saveSubmissionIcon(int id, Bitmap icon) throws Exception  {
		saveBitmap2(getSubmissionIconPath2(id), icon);
	}

	// ==================== UserIcon =====================
	public static String getUserIconPath2(int uid) {
		return FileStorage.getPath2(AVATAR_PATH + "/a"+uid);
	}

	public static Boolean checkUserIcon(int uid) {
		return FileStorage.fileExists2(getUserIconPath2(uid));
	}

	public static Bitmap loadUserIcon(int uid) {
		return loadBitmap2(getUserIconPath2(uid), 0);
	}

	public static void saveUserIcon(int uid, Bitmap icon) throws Exception {
		saveBitmap2(getUserIconPath2(uid), icon);
	}
	
	
	// ================ Bitmap processing =====================
	/**
	 * Attempts to load an icon from the specified place
	 * @param filename
	 * The filename of the icon e.g. avatars/a121212
	 * @return
	 */
	public static Bitmap loadBitmap2(String filename, int MaxSize) {
		FileInputStream is = null;
		Bitmap bitmap = null;
		try {
			is = FileStorage.getFileInputStream2(filename);
			if (is != null && is.available() > 0) {
				if (MaxSize > 0) {
					//Decode image size
			        BitmapFactory.Options o = new BitmapFactory.Options();
			        o.inJustDecodeBounds = true;
			        BitmapFactory.decodeStream(is, null, o);
					is.close();
					
					// calculate scale
					int imgMaxSize = Math.max(o.outHeight, o.outWidth);
					int scale = 1;

					while(true){ // hope some integer shift and compare operations is faster than Math.pow etc...
			            imgMaxSize >>= 1;
			            if( imgMaxSize < MaxSize)
			                break;
			            scale <<=1;
			        }

			        //Decode with inSampleSize
			        BitmapFactory.Options o2 = new BitmapFactory.Options();
			        o2.inSampleSize = scale;

					is = FileStorage.getFileInputStream2(filename);
					bitmap = BitmapFactory.decodeStream(is, null, o2);

				} else {
					bitmap = BitmapFactory.decodeStream(is);
				}
			} else {
				Log.w(AppConstants.TAG_STRING, "ImageStorage: Can't load from external storage");
			}
		} catch (Exception e) {
			Log.e(AppConstants.TAG_STRING, "ImageStorage: Error in loadIcon", e);
        } catch (java.lang.OutOfMemoryError om) {
			Log.e(AppConstants.TAG_STRING, "ImageStorage: Out of memory in loadIcon", om);
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
	private static void saveBitmap2(String filename, Bitmap icon) throws Exception{
		if (icon == null) throw new Exception("Attempt to store null icon for " + filename);
		FileOutputStream os = null;
		try {
			os = FileStorage.getFileOutputStream2(filename);
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
		FileStorage.cleanup2(FileStorage.getPath2(SUBMISSION_IMAGE_PATH + "/"));
	}
	
	
}
