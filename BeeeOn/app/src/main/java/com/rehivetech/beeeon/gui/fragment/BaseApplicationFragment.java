package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;

import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;

public abstract class BaseApplicationFragment extends TrackFragment {

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
