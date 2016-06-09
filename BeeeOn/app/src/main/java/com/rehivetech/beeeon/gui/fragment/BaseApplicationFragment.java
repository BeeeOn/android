package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;

import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;

import butterknife.Unbinder;
import icepick.Icepick;

public abstract class BaseApplicationFragment extends android.support.v4.app.Fragment {

	protected BaseApplicationActivity mActivity;
	@Nullable protected Unbinder mUnbinder;

	/**
	 * Attaches only to BaseApplicationActivity, other not allowed
	 *
	 * @param context activity context
	 */
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			mActivity = (BaseApplicationActivity) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must be subclass of BaseApplicationActivity");
		}
	}

	/**
	 * Handles restoring state with Icepick
	 *
	 * @param savedInstanceState old state
	 */
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Icepick.restoreInstanceState(this, savedInstanceState);
	}

	/**
	 * Handles saving state with Icepick
	 *
	 * @param outState new state
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Icepick.saveInstanceState(this, outState);
	}


	/**
	 * Unbinds butterknife if was present
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mUnbinder != null) {
			mUnbinder.unbind();
		}
	}
}
