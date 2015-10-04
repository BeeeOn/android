package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.model.LocationsModel;

import java.util.Collections;
import java.util.List;

public class LocationArrayAdapter extends ArrayAdapter<Location> {

	@NonNull
	List<Location> mLocations;

	public LocationArrayAdapter(@NonNull Context context, @NonNull List<Location> locations) {
		super(context, R.layout.activity_module_edit_spinner_item, R.id.location_label, locations);
		mLocations = Collections.unmodifiableList(locations);

		setDropDownViewResource(R.layout.activity_module_edit_spinner_dropdown_item);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.activity_module_edit_spinner_dropdown_item, parent, false);
		}
		return updateView(view, position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.activity_module_edit_spinner_item, parent, false);
		}
		return updateView(view, position);
	}

	private View updateView(View view, int position) {
		Location location = getItem(position);
		if (location == null)
			return view;

		ImageView icon = (ImageView) view.findViewById(R.id.location_icon);
		icon.setImageResource(location.getIconResource());

		TextView label = (TextView) view.findViewById(R.id.location_label);
		label.setText(location.getName());

		return view;
	}

	public void setLocations(@NonNull List<Location> locations) {
		clear();
		for (Location location : locations) {
			add(location);
		}
		mLocations = Collections.unmodifiableList(locations);
		notifyDataSetChanged();
	}

	public List<Location> getLocationsList() {
		return mLocations;
	}

	public static List<Location> getLocations(LocationsModel locationsModel, Context context, String gateId) {
		List<Location> locations = locationsModel.getLocationsByGate(gateId);

		// Add "missing" default rooms
		for (Location.DefaultLocation room : Location.DefaultLocation.values()) {
			String name = context.getString(room.getTitleResource());

			boolean found = false;
			for (Location location : locations) {
				if (location.getName().equals(name)) {
					found = true;
					break;
				}
			}

			if (!found) {
				locations.add(new Location(Location.NEW_LOCATION_ID, name, gateId, room.getId()));
			}
		}

		// Sort them
		Collections.sort(locations, new NameIdentifierComparator());

		// Add "New location" item
		locations.add(new Location(Location.NEW_LOCATION_ID, context.getString(R.string.adapter_location_array_new_location_spinner), gateId, "0"));

		return locations;
	}

}
