package cz.vutbr.fit.iha.activity.menuItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;

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
		TextView nameView = (TextView) view.findViewById(cz.vutbr.fit.iha.R.id.name);
		ImageView iconView = (ImageView) view.findViewById(cz.vutbr.fit.iha.R.id.icon);
		
		nameView.setText(mName);
		iconView.setImageResource(mIconRes);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_setting;
	}

	
	
}
