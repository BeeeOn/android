package com.rehivetech.beeeon.activity.menuItem;

import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class LocationMenuItem extends AbstractMenuItem {
	private String mName;
	private int mIconRes;
	private boolean mTopSeparatorVisible;
	private boolean mActualLoc;

	public LocationMenuItem(String name, int iconRes, boolean topSeparator, String id, boolean actualLoc) {
		super(id, MenuItemType.LOCATION);
		mName = name;
		mIconRes = iconRes;
		mTopSeparatorVisible = topSeparator;
		mActualLoc = actualLoc;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.name);
		ImageView iconView = (ImageView) view.findViewById(com.rehivetech.beeeon.R.id.icon);
		View separatorView = (View) view.findViewById(com.rehivetech.beeeon.R.id.top_separator);

		nameView.setText(mName);
		iconView.setImageResource(mIconRes);
		if (mTopSeparatorVisible) {
			separatorView.setVisibility(View.VISIBLE);
		} else {
			separatorView.setVisibility(View.GONE);
		}
		if (mActualLoc) {
			nameView.setTextColor(view.getResources().getColor(R.color.beeeon_primary_cyan));
			nameView.setTypeface(null, Typeface.BOLD);
		}
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_location;
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
