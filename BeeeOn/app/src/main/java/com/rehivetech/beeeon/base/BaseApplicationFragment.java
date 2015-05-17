package com.rehivetech.beeeon.base;

import android.app.Activity;

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
