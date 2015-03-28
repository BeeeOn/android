package com.rehivetech.beeeon.arrayadapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.location.Location;

public class LocationIconAdapter extends ArrayAdapter<Integer> {

	private List<Integer> mIcons;
	private int mLayoutResource;
	private int mDropDownLayoutResource;
	private Context mActivity;


	public LocationIconAdapter(Context context, int resource, List<Integer> objects) {
		super(context, resource, objects);
		mLayoutResource = resource;
		mIcons = objects;
		mActivity = context;
	}

	public LocationIconAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<Integer>());
		mLayoutResource = resource;
		mIcons = getIconArray();
		mActivity = context;
	}

	@Override
	public Integer getItem(int position) {
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
		icon.setImageResource(mIcons.get(position));

		return row;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(mLayoutResource, parent, false);

		ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_icon);
		icon.setImageResource(mIcons.get(position));

		return row;
	}

	public List<Integer> getIconArray() {
		// Prepare list of icons
		List<Integer> iconsList = new ArrayList<Integer>();
		for (Location.LocationIcon icon : Location.LocationIcon.values()) {
			iconsList.add(icon.getIconResource());
		}
		return iconsList;
	}


}
