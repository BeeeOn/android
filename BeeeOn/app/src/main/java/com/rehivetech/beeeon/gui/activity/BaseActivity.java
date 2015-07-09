package com.rehivetech.beeeon.gui.activity;

import android.support.v7.app.AppCompatActivity;

import com.google.analytics.tracking.android.EasyTracker;

public abstract class BaseActivity extends AppCompatActivity {

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
