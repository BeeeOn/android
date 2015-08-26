package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.MainSettingsFragment;

/**
 * Created by david on 26.8.15.
 */
public class SettingsActivity extends BaseApplicationActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_main);
		setupToolbar(R.string.settings_main_settings);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
		}

		getSupportFragmentManager().beginTransaction().replace(R.id.settings_activity_fragment_holder,new MainSettingsFragment()).commit();
	}
}
