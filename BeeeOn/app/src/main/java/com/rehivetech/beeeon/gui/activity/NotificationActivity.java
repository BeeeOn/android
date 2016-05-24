package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.fragment.NotificationFragment;

public class NotificationActivity extends BaseApplicationActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		setupToolbar(R.string.notification_title_notification, INDICATOR_BACK);

		Fragment fragment = new NotificationFragment();
		fragment.setArguments(getIntent().getExtras());

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, fragment)
					.commit();
		}
	}
}
