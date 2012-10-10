package com.sofurry.util;

import android.content.Context;
import android.util.TypedValue;

public class Utils {
	public static int dp_to_px(Context context, float dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
                dp, context.getResources().getDisplayMetrics());
	}
}
