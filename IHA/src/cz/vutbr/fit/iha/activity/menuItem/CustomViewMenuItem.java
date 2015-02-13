package cz.vutbr.fit.iha.activity.menuItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;

public class CustomViewMenuItem extends AbstractMenuItem {
	private String mName;
	private int mIconRes;
	private boolean mTopSeparatorVisible;
	private boolean mActualCustomView;

	public CustomViewMenuItem(String name, int iconRes, boolean topSeparator, String id, boolean active) {
		super(id, MenuItemType.CUSTOM_VIEW);
		mName = name;
		mIconRes = iconRes;
		mTopSeparatorVisible = topSeparator;
		mActualCustomView = active;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(cz.vutbr.fit.iha.R.id.name);
		ImageView iconView = (ImageView) view.findViewById(cz.vutbr.fit.iha.R.id.icon);
		View separatorView = (View) view.findViewById(cz.vutbr.fit.iha.R.id.top_separator);

		// view.setEnabled(false);
		// view.setOnClickListener(null);

		nameView.setText(mName);
		iconView.setImageResource(mIconRes);
		if (mTopSeparatorVisible) {
			separatorView.setVisibility(View.VISIBLE);
		} else {
			separatorView.setVisibility(View.GONE);
		}
		if (mActualCustomView) {
			nameView.setTextColor(view.getResources().getColor(R.color.iha_primary_cyan));
		}
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_custom_view;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor( getMView().getResources().getColor(R.color.light_gray));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor( getMView().getResources().getColor(R.color.iha_drawer_bg));
	}

}
