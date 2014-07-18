package cz.vutbr.fit.iha.activity.menuItem;

import cz.vutbr.fit.iha.R;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomViewMenuItem extends AbstractMenuItem {
	private String mName;
	private int mIconRes;
	private boolean mTopSeparatorVisible;
	
	public CustomViewMenuItem(String name, int iconRes, boolean topSeparator, String id) {
		super(id, MenuItemType.CUSTOM_VIEW);
		mName = name;
		mIconRes = iconRes;
		mTopSeparatorVisible = topSeparator;
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
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_custom_view;
	}

	
	
}
