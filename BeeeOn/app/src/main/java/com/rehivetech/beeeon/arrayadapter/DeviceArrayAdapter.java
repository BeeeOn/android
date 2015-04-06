package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;

import java.util.List;

public class DeviceArrayAdapter extends ArrayAdapter<Device> {

    private final List<Location> mLocations;
    private List<Device> mDevices;
	private int mLayoutResource;
	private int mDropDownLayoutResource;

	private LayoutInflater mInflater;

	public DeviceArrayAdapter(Context context, int resource, List<Device> objects, List<Location> locations) {
		super(context, resource, objects);
		mLayoutResource = resource;
        mDevices = objects;
        mLocations = locations;
	}

	@Override
	public void setDropDownViewResource(int resource) {
		mDropDownLayoutResource = resource;
	}

	public void setLayoutInflater(LayoutInflater li) {
		mInflater = li;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
        DropDownHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(mDropDownLayoutResource, parent, false);

            holder = new DropDownHolder();

            holder.ItemIcon = (ImageView) convertView.findViewById(R.id.custom_spinner2_dropdown_icon);
            holder.ItemLabel = (TextView) convertView.findViewById(R.id.custom_spinner2_dropdown_label);
            holder.ItemLocation = (TextView) convertView.findViewById(R.id.custom_spinner2_dropdown_sublabel);

            convertView.setTag(holder);
        }
        else{
            holder = (DropDownHolder) convertView.getTag();
        }

        Device device = mDevices.get(position);

		holder.ItemLabel.setText(device.getName());
        holder.ItemIcon.setImageResource(device.getIconResource());
        holder.ItemLocation.setText(mLocations.get(getLocationsIndexFromArray(device.getFacility().getLocationId())).getName());

		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = mInflater.inflate(mLayoutResource, parent, false);

        Device device = mDevices.get(position);

        TextView label = (TextView) row.findViewById(R.id.custom_spinner2_label);
		label.setText(device.getName());

		ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner2_icon);
		icon.setImageResource(device.getIconResource());

        TextView sublabel = (TextView) row.findViewById(R.id.custom_spinner2_sublabel);
        sublabel.setText(mLocations.get(getLocationsIndexFromArray(device.getFacility().getLocationId())).getName());

		return row;
	}

    private int getLocationsIndexFromArray(String locId) {
        int index = 0;
        for (Location room : mLocations) {
            if (room.getId().equalsIgnoreCase(locId)) {
                return index;
            }
            index++;
        }
        return index;
    }

    private static class DropDownHolder{
        public TextView ItemLabel;
        public ImageView ItemIcon;
        public TextView ItemLocation;
    }
}
