package com.sofurry.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.sofurry.model.Submission;

public class SubmissionGalleryAdapter extends BaseAdapter {
    private Context context;
	private ArrayList<Submission> items;
	private Drawable defaultImage = null;

    public SubmissionGalleryAdapter(Context c, ArrayList<Submission> items) {
    	super();
		this.items = items;
        context = c;

        Resources res = context.getResources();
		defaultImage = res.getDrawable(android.R.drawable.ic_popup_sync);
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return items.get(position).getId();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = null;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(5, 5, 5, 5);
        } else {
            imageView = (ImageView) convertView;
        }

        Bitmap thumb = items.get(position).getThumbnail();
        if (thumb != null)
          imageView.setImageBitmap(thumb);
        else
          imageView.setImageDrawable(defaultImage);
        return imageView;
    }
}