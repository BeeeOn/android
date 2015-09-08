package com.rehivetech.beeeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;

/**
 * Created by david on 3.9.15.
 */
public class ActualizationTime extends SettingsItem {
	public static final String PERSISTENCE_ACTUALIZATON_KEY = "pref_actualization";

	public static final int DO_NOT_ACTUALIZE = 0;
	public static final int FIVE_SECONDS = 5;
	public static final int TEN_SECONDS = 10;
	public static final int THIRTY_SECONDS = 30;
	public static final int SIXTY_SECONDS = 60;

	public ActualizationTime() {
		super();

		mItems.add(new BaseItem(DO_NOT_ACTUALIZE, R.string.actualizationtime_listpreference_dont_actualize));
		mItems.add(new BaseItem(FIVE_SECONDS, R.string.actualizationtime_listpreference_five_secs));
		mItems.add(new BaseItem(TEN_SECONDS, R.string.actualizationtime_listpreference_ten_secs));
		mItems.add(new BaseItem(THIRTY_SECONDS, R.string.actualizationtime_listpreference_thirty_secs));
		mItems.add(new BaseItem(SIXTY_SECONDS, R.string.actualizationtime_listpreference_sixty_secs));
	}

	@Override
	public int getDefaultId() {
		return DO_NOT_ACTUALIZE;
	}

	@Override
	public String getPersistenceKey() {
		return PERSISTENCE_ACTUALIZATON_KEY;
	}

	public static int getTimeFromPrefsInSecs(Context context) {
		SharedPreferences prefs = Controller.getInstance(context).getUserSettings();
		if (prefs == null)
			return 0; //do not actualize
		return Integer.parseInt(prefs.getString(PERSISTENCE_ACTUALIZATON_KEY, "0"));
	}
}
