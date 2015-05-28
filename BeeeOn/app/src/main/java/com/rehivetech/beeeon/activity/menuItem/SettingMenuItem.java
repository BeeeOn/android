package com.rehivetech.beeeon.activity.menuItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class SettingMenuItem extends AbstractMenuItem {
	private String mName;
	private int mIconRes;

	public SettingMenuItem(String name, int iconRes, String id) {
		super(id, MenuItemType.SETTING);
		mName = name;
		mIconRes = iconRes;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.name);
		ImageView iconView = (ImageView) view.findViewById(com.rehivetech.beeeon.R.id.icon);

		nameView.setText(mName);
		iconView.setImageResource(mIconRes);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_setting;
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
