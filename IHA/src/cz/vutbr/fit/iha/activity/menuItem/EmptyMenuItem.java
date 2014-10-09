package cz.vutbr.fit.iha.activity.menuItem;

import android.view.View;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;

public class EmptyMenuItem extends AbstractMenuItem {
	private String mName;

	public EmptyMenuItem(String name) {
		super(MenuItem.ID_UNDEFINED, MenuItemType.EMPTY);
		mName = name;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(cz.vutbr.fit.iha.R.id.name);
		nameView.setText(mName);

		view.setEnabled(false);
		view.setOnClickListener(null);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_empty;
	}

}
