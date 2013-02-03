package com.sofurry.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.model.Submission;
import com.sofurry.storage.FileStorage;
import com.sofurry.util.Utils;



public class SubmissionGalleryAdapter extends BaseAdapter {
    private Context context;
	private ArrayList<Submission> items;
	private Drawable defaultImage = null;
	private LayoutInflater mInflater;

	private static final boolean enableLayoutItems = true;

    public SubmissionGalleryAdapter(Context c, ArrayList<Submission> items) {
    	super();
		this.items = items;
        context = c;

        Resources res = context.getResources();
		defaultImage = res.getDrawable(android.R.drawable.ic_popup_sync);
		mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
    	Submission item = items.get(position);
    	
    	if (item != null)
    		return item.getId();
    	else
    		return -1;
    }

    // viewholder to store links to intrface elements
    private class ViewHolder {
        public ImageView image = null;
        public ImageView saved_indicator = null;
        public ImageView video_indicator = null;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder holder = null;
    	
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            holder = new ViewHolder();
            
            if (enableLayoutItems) {
            	convertView = mInflater.inflate(R.layout.art_gallery_item, parent, false); // null

            	holder.image = (ImageView) convertView.findViewById(R.id.art_gallery_item_image);
            	holder.saved_indicator = (ImageView) convertView.findViewById(R.id.art_gallery_item_save_indicator);
            	holder.video_indicator = (ImageView) convertView.findViewById(R.id.art_gallery_item_video_indicator);
            } else {
                convertView = new ImageView(context);
                
                holder.image = (ImageView) convertView;
            }
            
            convertView.setTag(holder);

            SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(context);
            int mThumbSize = Utils.dp_to_px(context, prefs.getInt(AppConstants.PREFERENCE_THUMB_SIZE, 130) );
//            float scale = context.getResources().getDisplayMetrics().density;
//            mThumbSize = (int) (mThumbSize * scale + 0.5f);
//            mThumbSize= mThumbSize; // +5

//            LayoutParams lp = convertView.getLayoutParams();
//            lp.
            convertView.setLayoutParams(new GridView.LayoutParams(mThumbSize, mThumbSize));
            holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.image.setAdjustViewBounds(true);
            
            int mPaddingInPixels = Utils.dp_to_px(context, 5);
/*            int mPaddingInPixels;
            mPaddingInPixels = (int) (2 * scale + 0.5f);
            mPaddingInPixels = mPaddingInPixels + 5;*/
            holder.image.setPadding(mPaddingInPixels, mPaddingInPixels, mPaddingInPixels, mPaddingInPixels);
            
/*            convertView.setDrawingCacheEnabled(true);
            convertView.buildDrawingCache();/**/
        } else {
        	holder = (ViewHolder) convertView.getTag();

            // release bitmap memory
            Drawable toRecycle = holder.image.getDrawable();
            if ((toRecycle != null) && (toRecycle != defaultImage)) {
            	if (toRecycle instanceof BitmapDrawable) {
            		((BitmapDrawable) toRecycle).getBitmap().recycle();
            	}
                holder.image.setImageBitmap(null);
            }
        }

        Submission item = items.get(position);

        Bitmap thumb = null;
        
        if (item != null)
            thumb = item.getThumbnail();

        if (thumb != null)
            holder.image.setImageBitmap(thumb);
          else
            holder.image.setImageDrawable(defaultImage);

        if (enableLayoutItems) {
        	try {
            	// set saved indicator
        		if ( (item != null) && (FileStorage.fileExists(item.getSaveName(context)))) {
        			holder.saved_indicator.setVisibility(View.VISIBLE);
        		} else {
        			holder.saved_indicator.setVisibility(View.INVISIBLE);
        		}

        		// set video indicator
        		if ( (item != null) && (item.isVideo())) {
        			holder.video_indicator.setVisibility(View.VISIBLE);
        		} else {
        			holder.video_indicator.setVisibility(View.INVISIBLE);
        		}
        	} catch (Exception e) {
        	}
        	
        	GradientDrawable bg = (GradientDrawable) holder.image.getBackground();
        	if ( (item == null) || (item.getContentLevel().equals("0"))) {
        		bg.setStroke(1, 0xFF1C1C1C);
        	} else {
        		bg.setStroke(1, 0xFF2C0000);
        	}
        }
        
        return convertView;
    }
}