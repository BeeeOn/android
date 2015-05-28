package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.location.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationIconAdapter extends ArrayAdapter<Location.LocationIcon> {

	private List<Location.LocationIcon> mIcons;
	private int mLayoutResource;
	private int mDropDownLayoutResource;
	private Context mActivity;


	public LocationIconAdapter(Context context, int resource, List<Location.LocationIcon> objects) {
		super(context, resource, objects);
		mLayoutResource = resource;
		mIcons = objects;
		mActivity = context;
	}

	public LocationIconAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<Location.LocationIcon>());
		mLayoutResource = resource;
		mIcons = getIconArray();
		mActivity = context;
	}

	@Override
	public Location.LocationIcon getItem(int position) {
		return mIcons.get(position);
	}

	@Override
	public int getCount() {
		return mIcons.size();
	}

	@Override
	public void setDropDownViewResource(int resource) {
		mDropDownLayoutResource = resource;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(mDropDownLayoutResource, parent, false);

		ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_dropdown_icon);
		icon.setImageResource(mIcons.get(position).getIconResource());

		return row;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(mLayoutResource, parent, false);

		ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_icon);
		icon.setImageResource(mIcons.get(position).getIconResource());

		return row;
	}

	public List<Location.LocationIcon> getIconArray() {
		// Prepare list of icons
		List<Location.LocationIcon> iconsList = new ArrayList<Location.LocationIcon>();
		for (Location.LocationIcon icon : Location.LocationIcon.values()) {
			iconsList.add(icon);
		}
		return iconsList;
	}


}
