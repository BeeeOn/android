package com.rehivetech.beeeon.activity.menuItem;

import android.view.View;
import com.rehivetech.beeeon.R;

public class SeparatorMenuItem extends AbstractMenuItem {

	public SeparatorMenuItem() {
		super(MenuItem.ID_UNDEFINED, MenuItemType.SEPARATOR);
	}

	@Override
	public void setView(View view) {
		// nothing to do, everything set in xml
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_separator;
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
