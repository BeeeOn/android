package com.rehivetech.beeeon.activity.spinnerItem;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.location.Location;

public class HeaderSpinnerItem extends AbstractSpinnerItem {
	private String mName;

	public HeaderSpinnerItem(String name) {
		super(ID_UNDEFINED, SpinnerItemType.HEADER);
		mName = name;
	}

	@Override
	public void setView(View convertView) {
		TextView ItemLabel = (TextView) convertView.findViewById(com.rehivetech.beeeon.R.id.name);
		ItemLabel.setText(mName);

		convertView.setEnabled(false);
		convertView.setOnClickListener(null);
		setMView(convertView);
	}

	@Override
	public String getObject() {
		return mName;
	}

	@Override
	public int getLayout() {
		return R.layout.custom_spinner_header;
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
