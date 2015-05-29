package com.rehivetech.beeeon.gui.activity;

//import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.support.v7.app.ActionBarActivity;

import com.google.analytics.tracking.android.EasyTracker;

public abstract class BaseActivity extends ActionBarActivity {

	@Override
	public void onStart() {
		super.onStart();

		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		EasyTracker.getInstance(this).activityStop(this);
	}

}
