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
    private Bitmap mLevel;
    private View.OnClickListener mListener;

	public ProfileMenuItem(String name, String email, Bitmap icon, View.OnClickListener listener) {
		super(MenuItem.ID_UNDEFINED, MenuItemType.PROFILE);
		mName = name;
		mEmail = email;
		mIcon = icon;
        mListener = listener;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.name);
		TextView emailView = (TextView) view.findViewById(com.rehivetech.beeeon.R.id.email);
		ImageView iconView = (ImageView) view.findViewById(com.rehivetech.beeeon.R.id.icon);
        ImageView levelView = (ImageView) view.findViewById(R.id.level);

		nameView.setText(mName);
		emailView.setText(mEmail);
		iconView.setImageBitmap(mIcon);

        view.setOnClickListener(mListener);
        emailView.setOnClickListener(mListener);
        iconView.setOnClickListener(mListener);
        nameView.setOnClickListener(mListener);
        levelView.setOnClickListener(mListener);
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
