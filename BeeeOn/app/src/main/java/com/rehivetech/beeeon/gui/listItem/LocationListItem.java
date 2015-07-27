package com.rehivetech.beeeon.gui.listItem;

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
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.list_location_header_text);
		nameView.setText(mName);
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.item_list_location_header;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.gray_light));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.beeeon_background));
	}

}
