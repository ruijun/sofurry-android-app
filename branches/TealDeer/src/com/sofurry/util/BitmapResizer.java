package com.sofurry.util;

import com.sofurry.AppConstants;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public class BitmapResizer {

	public static Bitmap resizeImage(Bitmap image, int maxWidth, int maxHeight) {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double imageAspect = (double) imageWidth / imageHeight;
		double canvasAspect = (double) maxWidth / maxHeight;
		double scaleFactor;

		if (imageAspect < canvasAspect) {
			scaleFactor = (double) maxHeight / imageHeight;
		} else {
			scaleFactor = (double) maxWidth / imageWidth;
		}

		float scaleWidth = ((float) scaleFactor) * imageWidth;
		float scaleHeight = ((float) scaleFactor) * imageHeight;
		Log.d(AppConstants.TAG_STRING, "BitmapResize: target size: " + maxWidth + "/" + maxHeight);
		Log.d(AppConstants.TAG_STRING, "BitmapResize: target aspect: " + canvasAspect);
		Log.d(AppConstants.TAG_STRING, "BitmapResize: image dimensions: " + imageWidth + "/" + imageHeight);
		Log.d(AppConstants.TAG_STRING, "BitmapResize: image aspect: " + imageAspect);
		Log.d(AppConstants.TAG_STRING, "BitmapResize: scaleFactor: " + scaleFactor);
		Log.d(AppConstants.TAG_STRING, "BitmapResize: Scaled dimensions: " + scaleWidth + "/" + scaleHeight);

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		return Bitmap.createScaledBitmap(image, (int) scaleWidth, (int) scaleHeight, true);
	}

}
