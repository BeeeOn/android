package com.rehivetech.beeeon.activity.listItem;

import android.view.View;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class LocationListItem extends AbstractListItem {
	private String mName;
	private int mIconRes;
	private boolean mTopSeparatorVisible;
	private boolean mActualLoc;

	public LocationListItem(String name, int iconRes, String id) {
		super(id, ListItemType.LOCATION);
		mName = name;
		mIconRes = iconRes;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.sensor_header_text);
		nameView.setText(mName);
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.sensor_listview_header;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.light_gray));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.beeeon_drawer_bg));
	}

}
