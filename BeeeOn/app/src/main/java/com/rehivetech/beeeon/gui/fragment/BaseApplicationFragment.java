package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;

import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;

public abstract class BaseApplicationFragment extends TrackFragment {

	protected BaseApplicationActivity mActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (BaseApplicationActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must be subclass of BaseApplicationActivity");
		}
	}

}
