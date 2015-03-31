package com.rehivetech.beeeon.activity.spinnerItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.listItem.AbstractListItem;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import org.w3c.dom.Text;

public class DeviceSpinnerItem extends AbstractSpinnerItem {
	private Device mDevice;
	private Location mLocation;

	public DeviceSpinnerItem(Device device, Location location, String id, Context context) {
		super(id, SpinnerItemType.DEVICE);
		mDevice = device;
		mLocation = location;
	}

	@Override
	public void setView(View itemView) {

		TextView ItemLabel = (TextView) itemView.findViewById(R.id.custom_spinner2_dropdown_label);
		TextView ItemSubLabel = (TextView) itemView.findViewById(R.id.custom_spinner2_dropdown_sublabel);
		ImageView ItemIcon = (ImageView) itemView.findViewById(R.id.custom_spinner2_dropdown_icon);

		// Set the results into TextViews
		ItemLabel.setText(mDevice.getName());

		if(mLocation != null) {
			ItemSubLabel.setText(mLocation.getName());
		}

		// Set the results into ImageView
		ItemIcon.setImageResource(mDevice.getIconResource());

		setMView(itemView);
	}

	@Override
	public Device getObject() {
		return mDevice;
	}

	@Override
	public int getLayout() {
		return R.layout.custom_spinner2_dropdown_item;
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
