package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationArrayAdapter extends ArrayAdapter<Location> {

	private Context mContext;
	private List<Location> mLocations;
	private int mDropDownLayoutResource;
	private int mViewLayoutResource;

	public LocationArrayAdapter(Context context, int resource, List<Location> objects) {
		super(context, resource, objects);
		mViewLayoutResource = resource;
		mLocations = objects;
		mContext = context.getApplicationContext();
	}

	public LocationArrayAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<Location>());
		mViewLayoutResource = resource;
		mContext = context.getApplicationContext();
		mLocations = getLocations();
	}

	@Override
	public Location getItem(int position) {
		return mLocations.get(position);
	}

	@Override
	public void setDropDownViewResource(int resource) {
		mDropDownLayoutResource = resource;
	}

	@Override
	public int getCount() {
		return mLocations.size();
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(mDropDownLayoutResource, parent, false);

		CheckedTextView label = (CheckedTextView) row.findViewById(R.id.custom_spinner_dropdown_label);
		label.setText(mLocations.get(position).getName());

		ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_dropdown_icon);
		int id = mLocations.get(position).getIconResource();
		icon.setImageResource(id);

		return row;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(mViewLayoutResource, parent, false);

		TextView label = (TextView) row.findViewById(R.id.custom_spinner_label);
		label.setText(mLocations.get(position).getName());

		ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon);
		icon.setImageResource(mLocations.get(position).getIconResource());

		return row;
	}

	public List<Location> getLocations() {
		// Get locations from gate
		List<Location> locations = new ArrayList<Location>();
		Controller controller = Controller.getInstance(mContext);
		Gate gate = controller.getActiveGate();
		if (gate != null) {
			locations = controller.getLocationsModel().getLocationsByGate(gate.getId());
		} else {
			// We need to have gate to continue below
			return locations;
		}

		// Add "missing" default rooms
		for (Location.DefaultLocation room : Location.DefaultLocation.values()) {
			String name = mContext.getString(room.getTitleResource());

			boolean found = false;
			for (Location location : locations) {
				if (location.getName().equals(name)) {
					found = true;
					break;
				}
			}

			if (!found) {
				locations.add(new Location(Location.NEW_LOCATION_ID, name, gate.getId(), room.getId()));
			}
		}

		// Sort them
		Collections.sort(locations, new NameIdentifierComparator());

		// Add "New location" item
		locations.add(new Location(Location.NEW_LOCATION_ID, mContext.getString(R.string.adapter_location_array_new_location_spinner), gate.getId(), "0"));

		return locations;
	}

}
