package com.rehivetech.beeeon.gui.menuItem;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class ProfileMenuItem extends AbstractMenuItem {
	private String mName;
	private String mEmail;
	private Bitmap mIcon;
	private View.OnClickListener mListener;

	public ProfileMenuItem(String name, String email, Bitmap icon, View.OnClickListener listener) {
		super(IMenuItem.ID_UNDEFINED, MenuItemType.PROFILE);
		mName = name;
		mEmail = email;
		mIcon = icon;
		mListener = listener;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(R.id.menu_profile_listview_name);
		TextView emailView = (TextView) view.findViewById(R.id.menu_profile_listview_email);
		ImageView iconView = (ImageView) view.findViewById(R.id.menu_profile_listview_icon);

		nameView.setText(mName);
		emailView.setText(mEmail);
		if (mIcon != null) {
			iconView.setImageBitmap(mIcon);
			iconView.setPadding(0, 0, 0, 0);
		}

		view.setOnClickListener(mListener);
		emailView.setOnClickListener(mListener);
		iconView.setOnClickListener(mListener);
		nameView.setOnClickListener(mListener);
	}

	@Override
	public int getLayout() {
		return R.layout.item_menu_profile_listview;
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
