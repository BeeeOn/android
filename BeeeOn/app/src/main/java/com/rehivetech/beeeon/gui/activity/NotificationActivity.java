package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.NotificationFragment;

public class NotificationActivity extends BaseApplicationActivity {

	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_notification);
			setSupportActionBar(mToolbar);
		}

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Fragment fragment = new NotificationFragment();
		fragment.setArguments(getIntent().getExtras());

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, fragment)
					.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
//				Intent upIntent = NavUtils.getParentActivityIntent(this);
//				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
//					// This activity is NOT part of this app's task, so create a new task
//					// when navigating up, with a synthesized back stack.
//					TaskStackBuilder.create(this)
//							// Add all of this activity's parents to the back stack
//							.addNextIntentWithParentStack(upIntent)
//									// Navigate up to the closest parent
//							.startActivities();
//				} else {
//					// This activity is part of this app's task, so simply
//					// navigate up to the logical parent activity.
//					NavUtils.navigateUpTo(this, upIntent);
//				}
				return true;
		}
		return false;
	}
}