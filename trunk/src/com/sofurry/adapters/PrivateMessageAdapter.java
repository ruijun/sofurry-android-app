package com.sofurry.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.sofurry.AppConstants;
import com.sofurry.R;
import com.sofurry.model.PrivateMessage;
import com.sofurry.viewwrappers.TwoLineIconViewWrapper;


/**
 * Adapter for Private Messages
 *
 * @author SoFurry
 */
public class PrivateMessageAdapter
        extends ArrayAdapter<PrivateMessage> {
    private ArrayList<PrivateMessage> items;
    private Context                   context;
    private Drawable icon_new;
    private Drawable icon_read;
    private Drawable icon_replied;


    //~--- constructors -------------------------------------------------------

    /**
     * Constructs the adapter
     *
     * @param context The context to which the adapter belongs
     * @param layoutResourceId The resource ID of the default layout used for rows
     * @param items The items that needs to be filled into the list
     */
    public PrivateMessageAdapter(Context context, int layoutResourceId, ArrayList<PrivateMessage> items) {
        super(context, layoutResourceId, items);

        this.items   = items;
        this.context = context;
        Resources res = context.getResources();
        icon_new = res.getDrawable(R.drawable.pm_new);
        icon_read = res.getDrawable(R.drawable.pm_read);
        icon_replied = res.getDrawable(R.drawable.pm_replied);

    }

    //~--- get methods --------------------------------------------------------

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View                   view    = convertView;
        TwoLineIconViewWrapper wrapper = null;
        PrivateMessage         m       = null;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view    = layoutInflater.inflate(R.layout.listitemtwolineicon, parent, false);
            wrapper = new TwoLineIconViewWrapper(view);

            view.setTag(wrapper);
        } else {
            wrapper = (TwoLineIconViewWrapper) view.getTag();
        }

        // Retrieve the element from the items array
        m = items.get(position);

        if (m != null) {
        	Drawable icon = null;
        	
        	if ((""+AppConstants.PM_STATUS_NEW).equals(m.getStatus()))
        		icon = icon_new;
        	else if ((""+AppConstants.PM_STATUS_READ).equals(m.getStatus()))
        		icon = icon_read;
        	else
        		icon = icon_replied;
        	
            wrapper.getIcon().setImageDrawable(icon);
            wrapper.getBottomText().setText("From: " + m.getFromUser());
            wrapper.getCenterText().setText("Date: " + m.getDate());
            wrapper.getTopText().setText(m.getSubject());
        }

        return view;
    }
}
