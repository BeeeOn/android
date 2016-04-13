package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;

import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;

public abstract class BaseApplicationFragment extends android.support.v4.app.Fragment{

	protected BaseApplicationActivity mActivity;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			mActivity = (BaseApplicationActivity) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString()+ " must be subclass of BaseApplicationActivity");
		}

		mActivity.onFragmentAttached(this);
	}

}
