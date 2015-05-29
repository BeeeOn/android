package com.rehivetech.beeeon.gui.menuItem;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.rehivetech.beeeon.R;

public class GroupImageMenuItem extends GroupMenuItem {
	private int mImgRes;
	private OnClickListener mListener;

	public GroupImageMenuItem(String name, int imagRes, OnClickListener imageClickListener) {
		super(name, MenuItemType.GROUP_IMAGE);
		mImgRes = imagRes;
		mListener = imageClickListener;
	}

	@Override
	public void setView(View view) {
		super.setView(view);
		ImageView imgView = (ImageView) view.findViewById(com.rehivetech.beeeon.R.id.image);
		imgView.setImageResource(mImgRes);
		imgView.setOnClickListener(mListener);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_group_image;
	}
}
