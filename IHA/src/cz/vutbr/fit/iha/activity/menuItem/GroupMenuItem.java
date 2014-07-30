package cz.vutbr.fit.iha.activity.menuItem;

import android.view.View;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;

public class GroupMenuItem extends AbstractMenuItem {
	private String mName;

	protected GroupMenuItem(String name, MenuItemType type) {
		super(MenuItem.ID_UNDEFINED, type);
		mName = name;
	}
	
	public GroupMenuItem(String name) {
		super(MenuItem.ID_UNDEFINED, MenuItemType.GROUP);
		mName = name;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view
				.findViewById(cz.vutbr.fit.iha.R.id.name);
		nameView.setText(mName);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_group;
	}

}
