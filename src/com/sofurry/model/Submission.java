package com.sofurry.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;

import com.sofurry.PreviewArtActivity;
import com.sofurry.util.ImageStorage;

public class Submission implements Serializable {

	private static final long serialVersionUID = -3841250259233075462L;

	public enum SUBMISSION_TYPE {ARTWORK, STORY, JOURNAL, MUSIC};
	
	private SUBMISSION_TYPE type;
	private int id = -1;
	private String name;
	private String content;
	private String tags;
	private String authorName;
	private String authorID;
	private String contentLevel;
	private String date;
	private String thumbnailUrl;
	private Bitmap thumbnail;
	
	public SUBMISSION_TYPE getType() {
		return type;
	}
	public void setType(SUBMISSION_TYPE type) {
		this.type = type;
	}
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
	public String getAuthorID() {
		return authorID;
	}
	public void setAuthorID(String authorID) {
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
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public Bitmap getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
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
		setAuthorID(datasource.getString("authorId"));
		setContentLevel(datasource.getString("contentLevel"));
		setTags(datasource.getString("keywords"));
		setThumbnailUrl(datasource.getString("thumb"));
	}
	
	/**
	 * Loads this Submission's Icon
	 */
	public void loadSubmissionIcon() {
		Bitmap thumb = ImageStorage.loadSubmissionIcon(getId());
		if (thumb != null)
			setThumbnail(thumb);
	}
	
	/**
	 * Loads the icon of this submissions owner (user)
	 */
	public void loadUserIcon() {
		Bitmap thumb = ImageStorage.loadUserIcon(Integer.parseInt(getAuthorID()));
		if (thumb != null)
			setThumbnail(thumb);
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
		intent.putExtra("thumbnail", getThumbnailUrl());

	}
	
	

	
}
