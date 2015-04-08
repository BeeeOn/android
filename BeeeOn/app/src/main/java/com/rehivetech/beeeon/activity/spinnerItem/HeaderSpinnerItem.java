package com.rehivetech.beeeon.activity.spinnerItem;

import android.view.View;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

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