package cz.vutbr.fit.iha.arrayadapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import cz.vutbr.fit.iha.R;

public class LocationIconAdapter extends ArrayAdapter<Integer> {

	private List<Integer> mIcons;
	private int mLayoutResource;
	private int mDropDownLayoutResource;

	private LayoutInflater mInflater;

	public LocationIconAdapter(Context context, int resource, List<Integer> objects) {
		super(context, resource, objects);
		mLayoutResource = resource;
		mIcons = objects;
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
		// LayoutInflater inflater = getLayoutInflater();
		View row = mInflater.inflate(mDropDownLayoutResource, parent, false);

		ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_dropdown_icon);
		icon.setImageResource(mIcons.get(position));

		return row;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// LayoutInflater inflater = getLayoutInflater();
		View row = mInflater.inflate(mLayoutResource, parent, false);

		ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_icon);
		icon.setImageResource(mIcons.get(position));

		return row;
	}

}
