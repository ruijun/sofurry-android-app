package com.sofurry.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.sofurry.util.ContentDownloader;
import com.sofurry.util.ImageStorage;

public class Submission implements Serializable, IHasThumbnail {

	private static final long serialVersionUID = -3841250259233075462L;

	public enum SUBMISSION_TYPE {ARTWORK, STORY, JOURNAL, MUSIC};

	
	private SUBMISSION_TYPE type;
	private int id = -1;
	private String name;
	private String content;
	private String tags;
	private String authorName;
	private int authorID;
	private String contentLevel;
	private String date;
	private String thumbnailUrl;
	private Bitmap thumbnail;
	
	private byte attempts = 0;
	
	public SUBMISSION_TYPE getType() {
		return type;
	}
	public void setType(SUBMISSION_TYPE type) {
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

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	/* (non-Javadoc)
	 * @see com.sofurry.model.IHasThumbnail#getThumbnail()
	 */
	public Bitmap getThumbnail() {
		return thumbnail;
	}
	
	public void loadIconFromStorage() {
		if (type == SUBMISSION_TYPE.ARTWORK) {
			thumbnail = ImageStorage.loadSubmissionIcon(getId());
		} else {
			thumbnail = ImageStorage.loadUserIcon(getAuthorID());
		}
	}
	
	public void storeIcon() {
		if (type == SUBMISSION_TYPE.ARTWORK) {
			ImageStorage.saveSubmissionIcon(getId(), thumbnail);
		} else {
			ImageStorage.saveUserIcon(getAuthorID(), thumbnail);
		}
	}
	
	/**
	 * Downloads the thumbnail for this submission
	 */
	public void populateThumbnail() throws Exception {
		if (getId() == -1) return;

		Log.i("SF ThumbDownloader", "Downloading thumb for pid " + getId() + " from " + thumbnailUrl);
		
		// See if we have the image in storage
		loadIconFromStorage();
		
		if (thumbnail == null)
		   thumbnail = ContentDownloader.downloadBitmap(thumbnailUrl);
		
		Log.i("SF ThumbDownloader", "Storing image");
		storeIcon();
	}
	
	/**
	 * Returns the number of attempts that were used to get the thumbnail
	 * @return
	 */
	public byte getThumbAttempts() {
		return attempts++;
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
		setThumbnailUrl(datasource.getString("thumb"));
	}
	
	public void storeSubmissionIcon() {
	}
	
	/**
	 * Adds the submissions extra data to an intent
	 * @param intent
	 */
	public void feedIntent(Intent intent) {
		intent.putExtra("name", getName());
		intent.putExtra("tags", getTags());
		intent.putExtra("authorName", getAuthorName());
		intent.putExtra("authorId", getAuthorID());
		intent.putExtra("thumbnail", thumbnailUrl);
	}
	
	

	
}
