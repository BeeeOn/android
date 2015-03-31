package com.rehivetech.beeeon.activity.spinnerItem;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.geofence.SimpleGeofence;

public class GeofenceSpinnerItem extends AbstractSpinnerItem {
	private SimpleGeofence mGeofence;

	public GeofenceSpinnerItem(SimpleGeofence geofence, String id, Context context) {
		super(id, SpinnerItemType.GEOFENCE);
		mGeofence = geofence;
	}

	@Override
	public void setView(View itemView) {

		TextView ItemLabel = (TextView) itemView.findViewById(android.R.id.text1);
		ItemLabel.setText(mGeofence.getName());

		setMView(itemView);
	}

	@Override
	public SimpleGeofence getObject() {
		return mGeofence;
	}

	@Override
	public int getLayout() {
		return android.R.layout.simple_spinner_dropdown_item;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor( getMView().getResources().getColor(R.color.light_gray));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor( getMView().getResources().getColor(R.color.beeeon_drawer_bg));
	}

}
