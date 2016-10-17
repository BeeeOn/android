package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.persistence.Persistence;

/**
 * @author martin
 * @since 03/09/16.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        String userId = Controller.getInstance(getContext()).getActualUser().getId();
        if (userId.isEmpty()) {
            //We can't work without userId
            return;
        }
        //Use own name for sharedPreferences
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(Persistence.getPreferencesFilename(userId));
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);
        ft.replace(R.id.settings_activity_fragment_holder, fragment, preferenceScreen.getKey());
        ft.addToBackStack(preferenceScreen.getKey());
        ft.commit();
        return true;
    }
}
