/*
 * @(#)ListItemTwoLineIconViewWrapper.java   2010-12-21
 *
 * Copyright (c) 2010 Blazing Skies
 */



package com.sofurry.viewwrappers;

//~--- non-JDK imports --------------------------------------------------------

import com.sofurry.R;

import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;


//~--- classes ----------------------------------------------------------------

/**
 * Functions as a ViewWrapper for the ListItemTwoLineIcon layout, as required
 * by the Holder pattern for list adapters.
 *
 *
 * @author AngelOD
 */
public class TwoLineIconViewWrapper {
    private ImageView icon_;
    private TextView  bottomText_;
    private TextView  centerText_;
    private TextView  topText_;
    private View      base_;


    //~--- constructors -------------------------------------------------------

    /**
     * Constructs ...
     *
     *
     * @param base
     */
    public TwoLineIconViewWrapper(View base) {
        base_ = base;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public TextView getBottomText() {
    	if (bottomText_ == null) {
    		bottomText_ = (TextView)base_.findViewById(R.id.bottomtext);
    	}
    	
        return bottomText_;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public TextView getCenterText() {
    	if (centerText_ == null) {
    		centerText_ = (TextView)base_.findViewById(R.id.centertext);
    	}
    	
        return centerText_;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public ImageView getIcon() {
    	if (icon_ == null) {
    		icon_ = (ImageView)base_.findViewById(R.id.icon);
    	}
    	
        return icon_;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public TextView getTopText() {
    	if (topText_ == null) {
    		topText_ = (TextView)base_.findViewById(R.id.toptext);
    	}
    	
        return topText_;
    }
}
