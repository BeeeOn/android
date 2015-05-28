package com.rehivetech.beeeon.activity.menuItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class GateMenuItem extends AbstractMenuItem {
	private String mName;
	private int mRole;
	private boolean mIsChosen;

	public GateMenuItem(String name, int resRole, boolean isChosen, String id) {
		super(id, MenuItemType.GATE);
		mName = name;
		mRole = resRole;
		mIsChosen = isChosen;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.name);
		TextView roleView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.role);
		ImageView iconView = (ImageView) view.findViewById(com.rehivetech.beeeon.R.id.icon);

		nameView.setText(mName);
		roleView.setText(mRole);
		if (mIsChosen) {
			iconView.setImageResource(R.drawable.ic_action_done);
			iconView.setVisibility(View.VISIBLE);
		} else {
			iconView.setVisibility(View.GONE);
		}
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_gate;
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
