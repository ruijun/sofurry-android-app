package com.sofurry.adapters;

import android.content.Context;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;

import com.sofurry.R;
import com.sofurry.model.Submission;
import com.sofurry.viewwrappers.TwoLineIconViewWrapper;

import java.util.ArrayList;


/**
 * Adapter that will display "submission" objects in a ListView
 *
 *
 * @author SoFurry
 */
public class SubmissionListAdapter
        extends ArrayAdapter<Submission> {
    private ArrayList<Submission> items;
    private Context               context;
    private Drawable              defaultImage = null;


    //~--- constructors -------------------------------------------------------

    /**
     * Constructs the adapter
     *
     *
     * @param context The context to which the adapter belongs
     * @param layoutResourceId The resource ID of the default layout used for rows
     * @param items The items that needs to be filled into the list
     */
    public SubmissionListAdapter(Context context, int layoutResourceId, ArrayList<Submission> items) {
        super(context, layoutResourceId, items);

        this.items   = items;
        this.context = context;

        Resources res = context.getResources();

        defaultImage = res.getDrawable(android.R.drawable.ic_popup_sync);
    }

    //~--- get methods --------------------------------------------------------

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View                   view       = convertView;
        Submission             submission = null;
        TwoLineIconViewWrapper wrapper    = null;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view    = layoutInflater.inflate(R.layout.listitemtwolineicon, parent, false);
            wrapper = new TwoLineIconViewWrapper(view);

            view.setTag(wrapper);
        } else {
            wrapper = (TwoLineIconViewWrapper) view.getTag();
        }

        // Retrieve submission from list of items
        submission = items.get(position);

        // If submission is found, fill in the views
        if (submission != null) {
        	// Set top/title text
            wrapper.getTopText().setText(submission.getName());

            // Retrieve icon and set it
            Bitmap icon = submission.getThumbnail();

            if (icon != null) {
                wrapper.getIcon().setImageBitmap(icon);
                wrapper.getIcon().setPadding(0, 0, 0, 0);
            } else {
                wrapper.getIcon().setImageDrawable(defaultImage);
            }

            // Construct center text and set it
            StringBuilder sb = new StringBuilder();

            if ((submission.getDate() != null) &&!submission.getDate().equalsIgnoreCase("null")) {
                sb.append(submission.getDate());
                sb.append(" - ");
            }

            if ((submission.getAuthorName() != null) &&!submission.getAuthorName().equalsIgnoreCase("null")) {
                sb.append(submission.getAuthorName());
            }

            wrapper.getCenterText().setText(sb.toString());

            // Set bottom text, if there are tags on the submission
            if ((submission.getTags() != null) &&!submission.getTags().equalsIgnoreCase("null")) {
                wrapper.getBottomText().setText(submission.getTags());
            } else {
            	wrapper.getBottomText().setText("");
            }
        }

        return view;
    }
}
