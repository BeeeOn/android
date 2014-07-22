package cz.vutbr.fit.iha;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import cz.vutbr.fit.iha.adapter.location.Location;

public class LocationArrayAdapter extends ArrayAdapter<Location> {
	
	private List<Location> mLocations;
	private int mLayoutResource;
	private int mDropDownLayoutResource;
	
	private LayoutInflater mInflater;

	public LocationArrayAdapter(Context context, int resource, List<Location> objects) {
		super(context, resource, objects);
		mLayoutResource = resource;
		mLocations = objects;
	}

	@Override
	public void setDropDownViewResource(int resource) {
		mDropDownLayoutResource = resource;
	}
	
	public void setLayoutInflater(LayoutInflater li) {
		mInflater = li;
	}

	@Override
	public View getDropDownView(int position, View convertView,	ViewGroup parent) {
		//LayoutInflater inflater = getLayoutInflater();
		View row = mInflater.inflate(mDropDownLayoutResource, parent, false);

		CheckedTextView label = (CheckedTextView) row
				.findViewById(R.id.custom_spinner_dropdown_label);
		label.setText(mLocations.get(position).getName());

		ImageView icon = (ImageView) row
				.findViewById(R.id.custom_spinner_dropdown_icon);
		int id = mLocations.get(position).getIconResource();
		icon.setImageResource(id);

		return row;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//LayoutInflater inflater = getLayoutInflater();
		View row = mInflater.inflate(mLayoutResource, parent, false);

		TextView label = (TextView) row
				.findViewById(R.id.custom_spinner_label);
		label.setText(mLocations.get(position).getName());

		ImageView icon = (ImageView) row
				.findViewById(R.id.custom_spinner_icon);
		icon.setImageResource(mLocations.get(position).getIconResource());

		return row;
	}

}
