package com.sofurry.model;

import java.io.File;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import com.sofurry.AppConstants;
import com.sofurry.base.interfaces.IHasThumbnail;
import com.sofurry.mobileapi.ApiFactory;
import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.downloaders.ContentDownloader;
import com.sofurry.storage.FileStorage;
import com.sofurry.storage.ImageStorage;
import com.sofurry.util.Utils;

public class Submission implements Serializable, IHasThumbnail {

	private static final long serialVersionUID = -3841250259233075462L;

	private ContentType type;
	private int id = -1;
	private String name;
	private String content;
	private String tags;
	private String authorName;
	private int authorID;
	private String contentLevel;
	private String date;
	private String SavedNameCache = "";
	public  String FileExt = "";
	private String saveFilename;

	private byte attempts = 0;
	
	public ContentType getType() {
		return type;
	}
	public void setType(ContentType type) {
		this.type = type;
	}
	/* (non-Javadoc)
	 * @see com.sofurry.model.IHasThumbnail#getId()
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public int getAuthorID() {
		return authorID;
	}
	public void setAuthorID(int authorID) {
		this.authorID = authorID;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContentLevel() {
		return contentLevel;
	}
	public void setContentLevel(String contentLevel) {
		this.contentLevel = contentLevel;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public String getSaveFilename() {
		return saveFilename;
	}
	public void setSaveFilename(String saveFilename) {
		this.saveFilename = saveFilename;
	}

//	public String getFilenameUrl() {
//		return filenameUrl;
//	}
	/* (non-Javadoc)
	 * @see com.sofurry.model.IHasThumbnail#getThumbnail()
	 */
	public Bitmap getThumbnail() {
//		return thumbnail;
		return loadIconFromStorage();
	}
	
	public Boolean checkThumbnail() {
		if (type == ContentType.art) {
			return ImageStorage.checkSubmissionIcon(getId());
		} else {
			return ImageStorage.checkUserIcon(getAuthorID());
		}
	} /**/

	public Bitmap loadIconFromStorage() {
		if (type == ContentType.art) {
			return ImageStorage.loadSubmissionIcon(getId());
		} else {
			return ImageStorage.loadUserIcon(getAuthorID());
		}
	}

	public String getThumbnailPath() {
		if (type == ContentType.art)
			return ImageStorage.getSubmissionIconPath(getId());
		else
			return ImageStorage.getUserIconPath(getAuthorID());
	}

	/*	public void storeIcon() throws Exception {
		if (type == SUBMISSION_TYPE.ARTWORK) {
			ImageStorage.saveSubmissionIcon(getId(), thumbnail);
		} else {
			ImageStorage.saveUserIcon(getAuthorID(), thumbnail);
		}
	} /**/
	
	/**
	 * Downloads the thumbnail for this submission
	 */
/*	public void populateThumbnail(boolean fastmode) throws Exception {
		if (getId() == -1) return;

		
		// See if we have the image in storage
		loadIconFromStorage();
		
		if (fastmode) return; // In fastmode we will not try downloading the thumb.
		
		if (thumbnail == null) {
		  Log.i(AppConstants.TAG_STRING, "ThumbDownloader: Downloading thumb for pid " + getId() + " from " + thumbnailUrl);
		  thumbnail = ContentDownloader.downloadBitmap(thumbnailUrl);
		  Log.i(AppConstants.TAG_STRING, "ThumbDownloader: Storing image");
		  storeIcon();
		}
	}/**/

	public void populateThumbnail(boolean fastmode) throws Exception {
		if (getId() == -1) return;
		if (fastmode) return; // In fastmode we will not try downloading the thumb.

		if (type == ContentType.art) {
			if (!ImageStorage.checkSubmissionIcon(getId()) ) {
				ContentDownloader.downloadFile(getThumbURL(), ImageStorage.getSubmissionIconPath(getId()), null, true);
			}
		} else {
			if (! ImageStorage.checkUserIcon(getAuthorID()) ) {
				ContentDownloader.downloadFile(getThumbURL(), ImageStorage.getUserIconPath(getAuthorID()), null, true);
			}
		}
			
	}
	
	/**
	 * Returns the number of attempts that were used to get the thumbnail
	 * @return
	 */
	public byte getThumbAttempts() {
		return attempts++;
	}
	
    /**
     * Build absolute file name to save 
     * @throws Exception 
     */
    public String getSaveName(Context context) throws Exception {
    	return getSaveName(context, null);
    }
    
    /**
     * Build absolute file name to save 
     * @throws Exception 
     */
    public String getSaveName(Context context, String suffix) throws Exception {
        // Image filename template (Issue 38) by NGryph
        // load template from preferences
    	if (suffix == null) // dont return cache for suffix name
    	if (SavedNameCache.length() > 0) {
    		return SavedNameCache;
    	}
    	
        SharedPreferences prefs        = Utils.getPreferences(context);
        String            fileNameTmpl = prefs.getString(AppConstants.PREFERENCE_IMAGE_FILE_NAME_TMPL,
                                                         "%AUTHOR% - %NAME%");
        boolean           useOnlyAdult = prefs.getBoolean(AppConstants.PREFERENCE_IMAGE_TMPL_USE_ONLY_ADULT, false);

        // filename must have correct extension. not sure is it true or not
//        String targetPath = fileNameTmpl + '.' + filename.substring(filename.lastIndexOf('.') + 1);
//        String targetPath = fileNameTmpl + HttpRequest.extractExtension(getSaveFilename());
        
        String targetPath = fileNameTmpl + ((suffix == null)?"":suffix) + FileExt;

        /*
         *  sanitizeFileName removes '/' character so separately sanitize every unsecure data field that comes from
         * the web. We're also making sure that dots are filtered out of these as well, simply because we want to
         * avoid accidental hacks due to weird names.
         */
        targetPath = targetPath.replaceAll("%AUTHOR%", FileStorage.sanitizeFileName(getAuthorName(), true));
        targetPath = targetPath.replaceAll("%NAME%", FileStorage.sanitizeFileName(getName(), true));
        targetPath = targetPath.replaceAll("%DATE%", FileStorage.sanitizeFileName(getDate(), true));
        targetPath = targetPath.replaceAll("%ID%", FileStorage.sanitizeFileName(""+getId(), true));

        // Determine the level
        if (getContentLevel().equals("0")) {
            targetPath = targetPath.replaceAll("%LEVEL%", "clean");
        } else if ((getContentLevel().equals("1")) || (useOnlyAdult)) {
            targetPath = targetPath.replaceAll("%LEVEL%", "adult");
        } else {
            targetPath = targetPath.replaceAll("%LEVEL%", "extreme");
        }

        // some kind of hack. getUserStoragePath performs sanitize on filename and broke '/' chars from template
        // let's treat that getUserStoragePath provide root dir for image lib when filename is empty
       	targetPath = FileStorage.getUserStoragePath("Images", "") + targetPath;

    	if (suffix == null)
    		SavedNameCache = targetPath; // do not cache name with suffix
       	return targetPath;
    }

    // build relative file name to look in cache
    public String getCacheName() {
//      String filename = getName() + HttpRequest.extractExtension(getThumbnailUrl());
        String filename = "content_" + getId() + FileExt;
        filename = FileStorage.sanitize(filename);
    	return filename;
    }

    public File getSubmissionFile() {
    	File f = null;
    	
    	try {
    		if (Utils.getPreferences().getBoolean(AppConstants.PREFERENCE_IMAGE_USE_LIB, false)) {
    			f = new File(getSaveName(null));
    		}
    	} catch (Exception e) {
		} 
    	
    	if ( (f == null) || (!f.exists())) {
    		f = new File(ImageStorage.getSubmissionImagePath(getCacheName()));
    		if (!f.exists()) {
    			return null;    // Until that file exists, there is nothing we can do really.
    		}
    	} 
    	
    	return f;
    }
    
    public boolean isSubmissionFileExists() {
    	return (getSubmissionFile() != null);
    }
    
    /**
     * Returns the preview URL
     * @return
     */
    public String getPreviewURL() {
		return ApiFactory.getPreviewURL(getId());
	}
	
    
	/**
	 * Returns the full URL
	 * @return
	 */
	public String getFullURL() {
		return ApiFactory.getFullURL(getId());
	}
	
	/**
	 * Returns the ThumbURL
	 * @return
	 */
	public String getThumbURL() {
		try {
			if (! Utils.getPreferences().getBoolean(AppConstants.PREFERENCE_USE_CUSTOM_THUMBS, true))
				return ApiFactory.getThumbURL(getId());
		} catch (Exception e) {
		}
		return ApiFactory.getCustomThumbURL(getId());
	}
	
	/**
	 * Populates the Submission with data from a JSON object
	 * @param datasource
	 * The object to extract the data from
	 * @throws Exception
	 */
	public void populate(JSONObject datasource) throws JSONException {
		setName(datasource.getString("name"));
		setId(Integer.parseInt(datasource.getString("pid")));
		setDate(datasource.getString("date"));
		setAuthorName(datasource.getString("authorName"));
		setAuthorID(Integer.parseInt(datasource.getString("authorId")));
		setContentLevel(datasource.getString("contentLevel"));
		setTags(datasource.getString("keywords"));
//		setThumbnailUrl(datasource.getString("thumb")); // thumbnails for video have different URL
		setSaveFilename(datasource.getString("thumb"));

		switch (datasource.getInt("contentType")) {
			case 0: 
				setType(ContentType.stories);
				break;
			case 1:
				setType(ContentType.art);
				break;
			case 2:
				setType(ContentType.music);
				break;
			case 3:
				setType(ContentType.journals);
				break;
			default:
				setType(ContentType.all);
		}
		
		switch (getType()) {
			case art: {
		        if (datasource.getString("thumb").contains("/video.png")) {
		        	FileExt = ".swf";
		        } else {
		        	FileExt = ".jpg";
		        };
		        break;
			}
			
			case music: {
				FileExt = ".mp3";
		        break;
			}
			
			case stories: {
				FileExt = ".txt";
		        break;
			}
			
			default:
				FileExt = "";
		}

//		if (type == SUBMISSION_TYPE.MUSIC) {
//		filenameUrl = datasource.getString("filename");
//	}

		
	}
	
	/**
	 * Adds the submissions extra data to an intent
	 * @param intent
	 */
	public void feedIntent(Intent intent) {
		intent.putExtra("submission", this);
		intent.putExtra("pageID", getId());
		intent.putExtra("name", getName());
		intent.putExtra("tags", getTags());
		intent.putExtra("authorName", getAuthorName());
		intent.putExtra("authorId", getAuthorID());
		intent.putExtra("thumbnail", getThumbURL());
		intent.putExtra("date", getDate());
		intent.putExtra("level", getContentLevel());
	}
	
    public boolean isImage() {
    	if (FileExt.equals(".jpg")) return true;
    	if (FileExt.equals(".jpeg")) return true;
    	if (FileExt.equals(".gif")) return true;
    	if (FileExt.equals(".png")) return true;
    	if (FileExt.equals(".bmp")) return true;
    	if (FileExt.equals(".tga")) return true;
    	return false;
    }

    public boolean isVideo() {
    	if (FileExt.equals(".swf")) return true;
    	if (FileExt.equals(".flv")) return true;
    	if (FileExt.equals(".avi")) return true;
    	if (FileExt.equals(".mpg")) return true;
    	if (FileExt.equals(".mkv")) return true;
    	if (FileExt.equals(".mov")) return true;
    	if (FileExt.equals(".asf")) return true;
    	if (FileExt.equals(".mpeg")) return true;
    	if (FileExt.equals(".wmv")) return true;
    	return false;
    }

	
}
