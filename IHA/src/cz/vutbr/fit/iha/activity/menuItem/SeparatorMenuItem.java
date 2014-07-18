package cz.vutbr.fit.iha.activity.menuItem;

import android.view.View;
import cz.vutbr.fit.iha.R;

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

}
