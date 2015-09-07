package com.rehivetech.beeeon.util;

import android.content.Context;

/**
 * Created by david on 3.9.15.
 */
public class ActualizationTime extends SettingsItem {
	public static final String PERSISTENCE_ACTUALIZATON_KEY = "pref_actualization";

	public static final int DO_NOT_ACTUALIZE = 0;
	public static final int FIVE_SECONDS = 1;
	public static final int TEN_SECONDS = 2;
	public static final int THIRTY_SECONDS = 3;
	public static final int SIXTY_SECONDS = 4;

	public ActualizationTime() {
		super();

		mItems.add(new BaseItem(DO_NOT_ACTUALIZE, 0));
		mItems.add(new BaseItem(FIVE_SECONDS, 0));
		mItems.add(new BaseItem(TEN_SECONDS, 0));
		mItems.add(new BaseItem(THIRTY_SECONDS, 0));
		mItems.add(new BaseItem(SIXTY_SECONDS, 0));
	}

	@Override
	public int getDefaultId() {
		return DO_NOT_ACTUALIZE;
	}

	@Override
	public String getPersistenceKey() {
		return PERSISTENCE_ACTUALIZATON_KEY;
	}
}
