package com.sofurry.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public class BitmapResizer {

	private static String sfapp = "SF BitmapResize";

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
		Log.d(sfapp, "target size: " + maxWidth + "/" + maxHeight);
		Log.d(sfapp, "target aspect: " + canvasAspect);
		Log.d(sfapp, "image dimensions: " + imageWidth + "/" + imageHeight);
		Log.d(sfapp, "image aspect: " + imageAspect);
		Log.d(sfapp, "scaleFactor: " + scaleFactor);
		Log.d(sfapp, "Scaled dimensions: " + scaleWidth + "/" + scaleHeight);

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		return Bitmap.createScaledBitmap(image, (int) scaleWidth, (int) scaleHeight, true);
	}

}
