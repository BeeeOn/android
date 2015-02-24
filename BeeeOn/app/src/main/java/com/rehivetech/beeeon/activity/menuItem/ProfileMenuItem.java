package com.rehivetech.beeeon.activity.menuItem;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.rehivetech.beeeon.R;

public class ProfileMenuItem extends AbstractMenuItem {
	private String mName;
	private String mEmail;
	private Bitmap mIcon;

	public ProfileMenuItem(String name, String email, Bitmap icon) {
		super(MenuItem.ID_UNDEFINED, MenuItemType.PROFILE);
		mName = name;
		mEmail = email;
		mIcon = icon;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.name);
		TextView emailView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.email);
		ImageView iconView = (ImageView) view.findViewById(com.rehivetech.beeeon.R.id.icon);

		nameView.setText(mName);
		emailView.setText(mEmail);
		iconView.setImageBitmap(mIcon);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_profile;
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
