package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.MainSettingsFragment;

import java.util.Locale;

/**
 * Created by david on 26.8.15.
 */
public class SettingsActivity extends BaseApplicationActivity implements MainSettingsFragment.onPreferenceChangedListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_main);
		setupToolbar(R.string.settings_main_settings);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
		}

		getSupportFragmentManager().beginTransaction().replace(R.id.settings_activity_fragment_holder, new MainSettingsFragment()).commit();
	}

	public void setLocale(String lang) {
		Locale locale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = locale;
		res.updateConfiguration(conf, dm);
		Intent refresh = new Intent(this, SettingsActivity.class);
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
