package com.sofurry.list;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sofurry.R;
import com.sofurry.model.PrivateMessage;

public class PrivateMessageAdapter extends ArrayAdapter<PrivateMessage> {

	private ArrayList<PrivateMessage> items;
	private Context context;

	public PrivateMessageAdapter(Context context, int textViewResourceId, ArrayList<PrivateMessage> items) {
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
		
		PrivateMessage m = items.get(position);
		
		if (m != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView ct = (TextView) v.findViewById(R.id.centertext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			if (tt != null) { 
				tt.setText(m.getSubject());
			}
			if (ct != null) {
				ct.setText("Date: " + m.getDate());
			}
			if (bt != null) { 
				bt.setText("From: " + m.getFromUser());
			}
		}
		
		return v;
	}
}