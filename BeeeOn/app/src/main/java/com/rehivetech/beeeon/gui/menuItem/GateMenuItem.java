package com.rehivetech.beeeon.gui.menuItem;

import android.view.View;
import android.widget.RadioButton;
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
		RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio_button);

		nameView.setText(mName);
		roleView.setText(mRole);
		if (mIsChosen) {
			radioButton.setChecked(true);
			nameView.setTextColor(view.getResources().getColor(R.color.beeeon_primary));
			view.setBackgroundColor(view.getResources().getColor(R.color.gray_light));
		} else {
			radioButton.setChecked(false);
		}
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_gate;
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
