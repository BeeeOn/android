package cz.vutbr.fit.iha.activity.menuItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;

public class AdapterMenuItem extends AbstractMenuItem {
	private String mName;
	private String mRole;
	private boolean mIsChosen;

	public AdapterMenuItem(String name, String role, boolean isChosen, String id) {
		super(id, MenuItemType.ADAPTER);
		mName = name;
		mRole = role;
		mIsChosen = isChosen;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(cz.vutbr.fit.iha.R.id.name);
		TextView roleView = (TextView) view.findViewById(cz.vutbr.fit.iha.R.id.role);
		ImageView iconView = (ImageView) view.findViewById(cz.vutbr.fit.iha.R.id.icon);

		nameView.setText(mName);
		roleView.setText(mRole);
		if (mIsChosen) {
			iconView.setImageResource(R.drawable.ic_action_done);
			iconView.setVisibility(View.VISIBLE);
		} else {
			iconView.setVisibility(View.GONE);
		}

	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_adapter;
	}

}
