package com.rehivetech.beeeon.gui.menuItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class LocationMenuItem extends AbstractMenuItem {
	private String mName;
	private int mIconRes;
	private int mIconResActive;
	private boolean mTopSeparatorVisible;
	private boolean mActualLoc;

	public LocationMenuItem(String name, int iconRes, int iconResActive, boolean topSeparator, String id, boolean actualLoc) {
		super(id, MenuItemType.LOCATION);
		mName = name;
		mIconRes = iconRes;
		mIconResActive = iconResActive;
		mTopSeparatorVisible = topSeparator;
		mActualLoc = actualLoc;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.name);
		ImageView iconView = (ImageView) view.findViewById(com.rehivetech.beeeon.R.id.icon);
		View separatorView = view.findViewById(R.id.top_separator);

		nameView.setText(mName);
		iconView.setImageResource(mIconRes);
		if (mTopSeparatorVisible) {
			separatorView.setVisibility(View.VISIBLE);
		} else {
			separatorView.setVisibility(View.GONE);
		}
		if (mActualLoc) {
			nameView.setTextColor(view.getResources().getColor(R.color.beeeon_primary));
			view.setBackgroundColor(view.getResources().getColor(R.color.gray_light));
			iconView.setImageResource(mIconResActive);
		} else {
			iconView.setImageResource(mIconRes);
		}
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.item_menu_location_listview;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.gray_light));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.beeeon_background_drawer));
	}

}
