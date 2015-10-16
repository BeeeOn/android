package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.analytics.Tracker;
import com.rehivetech.beeeon.BeeeOnApplication;


public abstract class BaseActivity extends AppCompatActivity {

	private Tracker mTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Obtain the shared Tracker instance.
		BeeeOnApplication application = (BeeeOnApplication) getApplication();
		mTracker = application.getDefaultTracker();
	}

}
