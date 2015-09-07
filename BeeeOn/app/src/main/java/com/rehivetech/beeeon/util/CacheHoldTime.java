package com.rehivetech.beeeon.util;

import android.content.Context;

/**
 * Created by david on 3.9.15.
 */
public class CacheHoldTime extends SettingsItem{
	public static final String PERSISTENCE_CACHE_KEY = "pref_cache";

	public static final int DO_NOT_ACTUALIZE = 0;
	public static final int ONE_MINUTE = 1;
	public static final int TWO_MINUTES = 2;
	public static final int FIVE_MINUTES = 3;
	public static final int TEN_MINUTES = 4;
	public static final int THIRY_MINUTES = 5;

	public CacheHoldTime() {
		super();

		mItems.add(new BaseItem(DO_NOT_ACTUALIZE, 0));
		mItems.add(new BaseItem(ONE_MINUTE,0));
		mItems.add(new BaseItem(TWO_MINUTES,0));
		mItems.add(new BaseItem(FIVE_MINUTES,0));
		mItems.add(new BaseItem(TEN_MINUTES,0));
		mItems.add(new BaseItem(THIRY_MINUTES,0));
	}

	@Override
	public int getDefaultId() {
		return DO_NOT_ACTUALIZE;
	}

	@Override
	public String getPersistenceKey() {
		return PERSISTENCE_CACHE_KEY;
	}
}
