package com.sofurry.requests;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sofurry.AppConstants;
import com.sofurry.model.Submission;
import com.sofurry.storage.FileStorage;
import com.sofurry.storage.ImageStorage;

// Load or Download image asyncronously then call onImageLoad
// onImageLoad MUST call mpp.recycle()
public class AsyncImageLoader extends Thread {
    private AsyncImageLoader.IImageLoadResult requesthandler = null;
	private Submission my_submission = null;
	private Context my_context = null;
	private Boolean forceDownload = false;
	private Boolean onlyDownload = false;
	private Boolean noScale = false;
	private Boolean cancelFlag = false;
	private int id = -1;
	private Handler mHandler = null;
	private Bitmap bmp = null;

	public interface IImageLoadResult {
		public void onImageLoad(int id, Object obj); // null = file downloaded no bitmap, exception - error happens, bitmap - loaded picture
	}
	
	public static AsyncImageLoader doLoad(Context con, AsyncImageLoader.IImageLoadResult req, Submission sub, Boolean forceDl, Boolean onlyDL, Boolean noScale) {
		  AsyncImageLoader dl = new AsyncImageLoader(con, req, sub, forceDl, onlyDL, noScale);
		  dl.start();
		  return dl;
	}
		
	public AsyncImageLoader(Context con, AsyncImageLoader.IImageLoadResult req, Submission sub, Boolean forceDl, Boolean onlyDL, Boolean noSc) {
			super();
			
			this.mHandler = new Handler();
			
			this.my_context = con;
			this.my_submission = sub;
			this.forceDownload = forceDl;
			this.onlyDownload = onlyDL;
			this.noScale = noSc;
			this.requesthandler = req;
			this.id = my_submission.getId();
	}

	public void doCancel() {
		cancelFlag = true;
		mHandler = null;
		requesthandler = null;
	}
	
	public void setOnlyDL(Boolean aOnlyDL) {
		onlyDownload = aOnlyDL;
	}
	
	private void send_result(final Object ares) {
		if (mHandler != null) {
            mHandler.post(new Runnable() {
                public void run() {
              	  if (requesthandler != null) {
              		  requesthandler.onImageLoad(id, ares);
              	  }
                }
            });
		}
	}
    public void run() {
        try {
//        	Bitmap b = null;
        	
        	// clean bmp in case of reuse (must newer happens but...)
        	if (bmp != null) {
        		bmp.recycle();
        		bmp = null;
        	}
        	
            // max size for loaded image. it must be larger than biggest side of screen to fit well even if we rotate screen
            int maxsize = 0;
            if (! noScale) 
            	maxsize = Math.max(	my_context.getApplicationContext().getResources().getDisplayMetrics().heightPixels, 
            						my_context.getApplicationContext().getResources().getDisplayMetrics().widthPixels);
            
        	SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(my_context);
            
        	// force DL - download file anyway
        	// only DL - download if not exists, do not load to memory
            if (forceDownload) {
            	forceDownload = false;
            } else {
            	if (onlyDownload) {
            		// if files already in cache/library then exit
                	if ( ((prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_USE_LIB, false)) && (FileStorage.fileExists2(my_submission.getSaveName(my_context)))) || (FileStorage.fileExists2(ImageStorage.getSubmissionImagePath2(my_submission.getCacheName())))) {
                    	send_result(null); // close progress bar
                		return;
                	}
                	
            	} else {
            		// try to load images from cache/library
                	if (prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_USE_LIB, false)) {
                    	bmp = ImageStorage.loadBitmap2(my_submission.getSaveName(my_context), maxsize);
                	}
                	
                	if (bmp == null) {
                		bmp = ImageStorage.loadSubmissionImage(my_submission.getCacheName(), maxsize);
                	}
            	}
            }

            if (bmp == null) {
            	String url;

            	if (prefs.getString(AppConstants.PREFERENCE_USE_HD_IMAGES, "unk").equals("1")) {
                    url = my_submission.getFullURL();
            	} else {
            		url = my_submission.getPreviewURL();
            	}

                Log.i(AppConstants.TAG_STRING, "ImageDownloader: Downloading image for id " + my_submission.getId() + " from " + url);

                ContentDownloader.downloadFile2(url, ImageStorage.getSubmissionImagePath2(my_submission.getCacheName()), null);

                if ((!cancelFlag)&&(!onlyDownload)) { // if image load was cancelled or onlyDL then do not load bitmap in memory, just put file to cache for future use 
                	// read file
                	bmp = ImageStorage.loadSubmissionImage(my_submission.getCacheName(), maxsize);
                	if (bmp == null) {
                		throw new Exception("Downloaded Image failed to load.");
                	}
                }
            }

            if ((cancelFlag) || (mHandler == null) || (requesthandler == null)) {
            	if (bmp != null) {
            		bmp.recycle();
            		bmp = null;
            	}
            } else {
                // Send bitmap to our hungry thread
//              requesthandler.postMessage(AppConstants.REQUEST_ID_ASYNCDOWNLOADIMAGE, res);
            	send_result(bmp);
            }

        } catch (Exception e) {
//            requesthandler.postMessage(AppConstants.REQUEST_ID_ASYNCDOWNLOADIMAGE, e);
        	send_result(e); // close progress bar
        } catch (java.lang.OutOfMemoryError om) {
//            requesthandler.postMessage(AppConstants.REQUEST_ID_ASYNCDOWNLOADIMAGE, om);
        	send_result(om); // close progress bar
        } /*finally {
        	// clear reference. onImageLoad MUST call mpp.recycle()
        	bmp = null;
        }/**/
    }

}
