package com.rehivetech.beeeon.gui.menuItem;

import android.view.View;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class SettingMenuItem extends AbstractMenuItem {
	private String mName;

	public SettingMenuItem(String name,  String id) {
		super(id, MenuItemType.SETTING);
		mName = name;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(R.id.menu_setting_listview_name);
		nameView.setText(mName);
	}

	@Override
	public int getLayout() {
		return R.layout.item_menu_setting_listview;
	}

	@Override
	public void setIsSelected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNotSelected() {
		// TODO Auto-generated method stub

	}

}
