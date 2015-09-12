package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.MainSettingsFragment;

/**
 * Created by david on 26.8.15.
 */
public class SettingsMainActivity extends BaseApplicationActivity implements MainSettingsFragment.OnPreferenceChangedListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_main);
		setupToolbar(R.string.settings_main_settings);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
		}

		if (!Controller.getInstance(this).isLoggedIn()) {
			// We need user to get his preferences
			finish();
			return;
		}

		getSupportFragmentManager().beginTransaction().replace(R.id.settings_activity_fragment_holder, new MainSettingsFragment()).commit();
	}

	// refresh activity after user change language so the change can be seen immediately
	public void refreshActivity() {
		Intent refresh = new Intent(this, SettingsMainActivity.class);
		refresh.setFlags(refresh.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
		startActivity(refresh);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

}
