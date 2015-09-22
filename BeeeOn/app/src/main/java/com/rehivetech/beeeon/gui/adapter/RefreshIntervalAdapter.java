package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rehivetech.beeeon.household.device.RefreshInterval;

/**
 * Created by Robyer on 20.09.2015.
 */
public class RefreshIntervalAdapter extends ArrayAdapter<RefreshInterval> {

	public RefreshIntervalAdapter(Context context) {
		super(context, android.R.layout.simple_spinner_item, RefreshInterval.values());
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = super.getDropDownView(position, convertView, parent);

		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setText(getItem(position).getStringInterval(getContext()));

		return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setText(getItem(position).getStringInterval(getContext()));

		return view;
	}
}
