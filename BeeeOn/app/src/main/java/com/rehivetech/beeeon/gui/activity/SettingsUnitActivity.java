package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.SettingsUnitFragment;

/**
 * Created by david on 14.9.15.
 */
public class SettingsUnitActivity extends BaseApplicationActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_units);
		setupToolbar(R.string.settings_unit_unit);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
		}

		if (!Controller.getInstance(this).isLoggedIn()) {
			// We need user to get his preferences
			finish();
			return;
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.settings_units_activity_fragment_holder, new SettingsUnitFragment()).commit();
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
