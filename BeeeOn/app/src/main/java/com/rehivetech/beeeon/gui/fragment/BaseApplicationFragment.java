package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;

import icepick.Icepick;

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

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Icepick.restoreInstanceState(this, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Icepick.saveInstanceState(this, outState);
	}
}
