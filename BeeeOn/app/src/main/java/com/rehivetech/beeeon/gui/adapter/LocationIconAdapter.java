package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.location.Location;

public class LocationIconAdapter extends ArrayAdapter<Location.LocationIcon> {

	public LocationIconAdapter(Context context) {
		super(context, R.layout.activity_module_edit_custom_spinner_icon_item, Location.LocationIcon.values());
		setDropDownViewResource(R.layout.activity_module_edit_spinner_icon_dropdown_item);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = super.getDropDownView(position, convertView, parent);
		return updateView(view, position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		return updateView(view, position);
	}

	private View updateView(View view, int position) {
		ImageView icon = (ImageView) view.findViewById(R.id.module_edit_custom_spinner_icon_dropdown_imageview);
		icon.setImageResource(getItem(position).getIconResource(IconResourceType.DARK));
		return view;
	}
}
