package com.sofurry.list;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sofurry.R;
import com.sofurry.model.Submission;

public class SubmissionAdapter extends ArrayAdapter<Submission> {

	private ArrayList<Submission> items;
	private Context context;

	public SubmissionAdapter(Context context, int textViewResourceId, ArrayList<Submission> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.listitemtwolineicon, parent, false);
		}
		Submission s = items.get(position);
		if (s != null) {
			TextView titletext = (TextView) v.findViewById(R.id.toptext);
			TextView centertext = (TextView) v.findViewById(R.id.centertext);
			TextView bottomtext = (TextView) v.findViewById(R.id.bottomtext);
			if (titletext != null) {
				titletext.setText(s.getName());
			}
			if (centertext != null) {
				StringBuilder sb = new StringBuilder();
				if (s.getDate() != null && !s.getDate().equalsIgnoreCase("null")) {
					sb.append(s.getDate());
					sb.append(" - ");
				}
				if (s.getAuthorName() != null && !s.getAuthorName().equalsIgnoreCase("null")) {
					sb.append(s.getAuthorName());
				}
				centertext.setText(sb.toString());
			}
			if (bottomtext != null) {
				if (s.getTags() != null && !s.getTags().equalsIgnoreCase("null"))
					bottomtext.setText(s.getTags());
				else
					bottomtext.setText("");
			}
		}
		return v;
	}
}