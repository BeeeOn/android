package com.rehivetech.beeeon.gui.menuItem;

import android.view.View;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class EmptyMenuItem extends AbstractMenuItem {
	private String mName;

	public EmptyMenuItem(String name) {
		super(MenuItem.ID_UNDEFINED, MenuItemType.EMPTY);
		mName = name;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.name);
		nameView.setText(mName);

		view.setEnabled(false);
		view.setOnClickListener(null);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_empty;
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
