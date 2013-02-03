package com.sofurry.mobileapi.downloaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sofurry.AppConstants;
import com.sofurry.model.Submission;
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
	private Boolean onlyCache = false;
	private DownloadCancler dlcancel = new DownloadCancler();
	
	public interface IImageLoadResult {
		public void onImageLoad(int id, Object obj); // null = file downloaded no bitmap, exception - error happens, bitmap - loaded picture
	}
	
	/**
	 * Load submission image in to bitmap.
	 * onlyDL + onlyCached = null (nothing to do)
	 * @param con - context to get preferences
	 * @param req - callback IImageLoadResult
	 * @param sub - submission which data should be downloaded
	 * @param forceDl - force download image even if it is in cache or stored
	 * @param onlyDL - do not create bitmap object in memory. check/download image only.
	 * @param noScale - do not try to scale image to fit in memory. Just give up if it does not fit.
	 * @param onlyCached - forbid download. Only load from local cache. Overrides forceDL
	 * @return -  AsyncImageLoader
	 */
	public static AsyncImageLoader doLoad(Context con, AsyncImageLoader.IImageLoadResult req, Submission sub, Boolean forceDl, Boolean onlyDL, Boolean noScale, Boolean onlyCached) {
		  if (sub == null)
			  return null;
		  
		  if (onlyDL && onlyCached) // nothing to do (do not load in memory && do not download file)
			  return null;
		  
		  AsyncImageLoader dl = new AsyncImageLoader(con, req, sub, forceDl, onlyDL, noScale, onlyCached);
		  if (dl != null)
			  dl.start();
		  return dl;
	}
		
	public AsyncImageLoader(Context con, AsyncImageLoader.IImageLoadResult req, Submission sub, Boolean forceDl, Boolean onlyDL, Boolean noSc, Boolean onlyCached) {
			super();
			
			this.mHandler = new Handler();
			
			this.my_context = con;
			this.my_submission = sub;
			this.forceDownload = forceDl;
			this.onlyDownload = onlyDL;
			this.noScale = noSc;
			this.requesthandler = req;
			this.id = my_submission.getId();
			this.onlyCache = onlyCached;
	}

	public int getSubmissionId() {
		return id;
	}
	
	public boolean getOnlyDl() {
		return onlyDownload;
	}
	
	public void doCancel() {
		cancelFlag = true;
		mHandler = null;
		requesthandler = null;
		dlcancel.doCancel();
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
        	if (my_submission == null)
        		throw new Exception("AsyncImageLoader: no submission assigned to load request");

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
/*            if ( (forceDownload) && (! onlyCache)) {
            	forceDownload = false;
            } else {/**/
        	if ((! forceDownload) || (onlyCache)) { // do not try to load first if forceDL. 
            	if (onlyDownload) {
            		// we are not expected to load any data to memory, prepare files only
            		// if we are forbidden to download or files already in cache/library then exit
                	if ( (onlyCache) || // do not download
                		 (my_submission.isSubmissionFileExists()) // already downloaded	
//                		 ((prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_USE_LIB, false)) && (FileStorage.fileExists(my_submission.getSaveName(my_context)))) || 
//                		 (FileStorage.fileExists(ImageStorage.getSubmissionImagePath(my_submission.getCacheName())))
                		) {
                    	send_result(null); // close progress bar
                		return;
                	}
                	
            	} else {
            		// try to load images from cache/library
                	if (prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_USE_LIB, false)) {
                    	bmp = ImageStorage.loadBitmap(my_submission.getSaveName(my_context), maxsize);
                	}
                	
                	if (bmp == null) {
                		bmp = ImageStorage.loadSubmissionImage(my_submission.getCacheName(), maxsize);
                	}
            	}
            	
            	// if file exists but can not be loaded on previous step then exit 
            	// as it can be concurrent download from other thread or non image submission file.
            	if ((bmp == null) && (my_submission.isSubmissionFileExists())) {
                	send_result(null); // close progress bar
            		return;
            	}
            }

            // do download to cache and then try to load downloaded file to mem. Only cache prohibit download.
            if ((bmp == null)&&(! onlyCache)) {
            	String url;

            	if (prefs.getString(AppConstants.PREFERENCE_USE_HD_IMAGES, "unk").equals("1")) {
                    url = my_submission.getFullURL();
            	} else {
            		url = my_submission.getPreviewURL();
            	}

                Log.i(AppConstants.TAG_STRING, "ImageDownloader: Downloading image for id " + my_submission.getId() + " from " + url);

                ContentDownloader.downloadFile(url, ImageStorage.getSubmissionImagePath(my_submission.getCacheName()), null, dlcancel, prefs.getBoolean(AppConstants.PREFERENCE_DELETE_INCOMPLETE, true));

                if (my_submission.isImage()) // do not try to load bmp for non-image files
                if ((!cancelFlag)&&(!onlyDownload)) { // if image load was cancelled or onlyDL then do not load bitmap in memory, just put file to cache for future use 
                	// read file
                	bmp = ImageStorage.loadSubmissionImage(my_submission.getCacheName(), maxsize);
                	if (bmp == null) {
                		throw new Exception("Download ok. Can't view. Try external viewer");
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
