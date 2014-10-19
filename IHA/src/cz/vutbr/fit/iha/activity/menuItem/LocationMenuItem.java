package cz.vutbr.fit.iha.activity.menuItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;

public class LocationMenuItem extends AbstractMenuItem {
	private String mName;
	private int mIconRes;
	private boolean mTopSeparatorVisible;
	private boolean mActualLoc;

	public LocationMenuItem(String name, int iconRes, boolean topSeparator, String id, boolean actualLoc) {
		super(id, MenuItemType.LOCATION);
		mName = name;
		mIconRes = iconRes;
		mTopSeparatorVisible = topSeparator;
		mActualLoc = actualLoc;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(cz.vutbr.fit.iha.R.id.name);
		ImageView iconView = (ImageView) view.findViewById(cz.vutbr.fit.iha.R.id.icon);
		View separatorView = (View) view.findViewById(cz.vutbr.fit.iha.R.id.top_separator);

		nameView.setText(mName);
		iconView.setImageResource(mIconRes);
		if (mTopSeparatorVisible) {
			separatorView.setVisibility(View.VISIBLE);
		} else {
			separatorView.setVisibility(View.GONE);
		}
		if(mActualLoc) {
			nameView.setTextColor(view.getResources().getColor(R.color.iha_primary_cyan));
		}
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_location;
	}

}
