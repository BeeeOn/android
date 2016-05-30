package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.SettingsMainFragment;

/**
 * Created by david on 26.8.15.
 */
public class SettingsMainActivity extends BaseApplicationActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_main);
		setupToolbar(R.string.settings_main_settings, INDICATOR_BACK);

		if (!Controller.getInstance(this).isLoggedIn()) {
			// We need user to get his preferences
			finish();
			return;
		}

		getSupportFragmentManager().beginTransaction().replace(R.id.settings_activity_fragment_holder, new SettingsMainFragment()).commit();
	}
}
