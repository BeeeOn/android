package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.fragment.SettingsFragment;

/**
 * @author david
 * @author martin
 * @since 26.8.15.
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

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_activity_fragment_holder, fragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.SETTINGS_SCREEN);
    }
}
