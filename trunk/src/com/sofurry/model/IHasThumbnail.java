package com.sofurry.model;

import android.graphics.Bitmap;

/**
 * @author Rangarig
 * 
 * Interface to be used by thumbnaildownloader for submissions or anything else that has a thumbnail
 */
public interface IHasThumbnail {

	Bitmap getThumbnail();
	void populateThumbnail() throws Exception;
	byte getThumbAttempts();

}