package com.rehivetech.beeeon.util;

import com.rehivetech.beeeon.R;

/**
 * Created by david on 3.9.15.
 */
public class CacheHoldTime extends SettingsItem {
	public static final String PERSISTENCE_CACHE_KEY = "pref_cache";

	public static final int DO_NOT_ACTUALIZE = 0;
	public static final int ONE_MINUTE = 60;
	public static final int TWO_MINUTES = 2 * 60;
	public static final int FIVE_MINUTES = 5 * 60;
	public static final int TEN_MINUTES = 10 * 60;
	public static final int THIRY_MINUTES = 30 * 30;

	public CacheHoldTime() {
		super();

		mItems.add(new BaseItem(DO_NOT_ACTUALIZE, R.string.cache_listpreference_do_not_store));
		mItems.add(new BaseItem(ONE_MINUTE, R.string.cache_listpreference_one_minute));
		mItems.add(new BaseItem(TWO_MINUTES, R.string.cache_listpreference_two_minutes));
		mItems.add(new BaseItem(FIVE_MINUTES, R.string.cache_listpreference_five_minutes));
		mItems.add(new BaseItem(TEN_MINUTES, R.string.cache_listpreference_ten_minutes));
		mItems.add(new BaseItem(THIRY_MINUTES, R.string.cache_listpreference_thirty_minutes));
	}

	@Override
	public int getDefaultId() {
		return FIVE_MINUTES;
	}

	@Override
	public String getPersistenceKey() {
		return PERSISTENCE_CACHE_KEY;
	}
}
