package com.rehivetech.beeeon.gui.menuItem;

import android.view.View;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class GroupMenuItem extends AbstractMenuItem {
	private String mName;

	protected GroupMenuItem(String name, MenuItemType type) {
		super(IMenuItem.ID_UNDEFINED, type);
		mName = name;
	}

	public GroupMenuItem(String name) {
		super(IMenuItem.ID_UNDEFINED, MenuItemType.GROUP);
		mName = name;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(R.id.menu_group_listview_name);
		nameView.setText(mName);

		view.setEnabled(false);
		view.setOnClickListener(null);
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.item_menu_group_listview;
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
