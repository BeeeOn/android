package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragmentCompat;

import com.rehivetech.beeeon.R;

/**
 * Created by david on 26.8.15.
 */
public class MainSettingsFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		addPreferencesFromResource(R.xml.activity_settings_main_preferences);
	}
}
