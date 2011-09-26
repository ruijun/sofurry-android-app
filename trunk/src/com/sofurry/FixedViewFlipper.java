package com.sofurry;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

public class FixedViewFlipper extends ViewFlipper {
	public boolean captureAllTouch = false;
	
	public FixedViewFlipper(Context context) {
        super(context);
    }

    public FixedViewFlipper(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }
    
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (captureAllTouch) { 
			return true;
		} else {
			return super.onInterceptTouchEvent(ev);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
	    try {
	        super.onDetachedFromWindow();
	    }
	    catch (IllegalArgumentException e) {
	        stopFlipping();
	    }
	}
}
