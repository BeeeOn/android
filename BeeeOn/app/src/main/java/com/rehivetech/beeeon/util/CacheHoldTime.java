package com.rehivetech.beeeon.util;

import android.content.Context;

import com.rehivetech.beeeon.R;

/**
 * Created by david on 3.9.15.
 */
public class CacheHoldTime extends SettingsItem {
	public static final String PERSISTENCE_CACHE_KEY = "pref_cache";

	public static final int NO_CACHE = 0;
	public static final int ONE_MINUTE = 1;
	public static final int TWO_MINUTES = 2;
	public static final int FIVE_MINUTES = 5;
	public static final int TEN_MINUTES = 10;
	public static final int THIRY_MINUTES = 30;

	public class Item extends BaseItem {
		private final int mMinutes;

		protected Item(int minutes) {
			super(minutes, 0); // Using minutes as ID for simplicity (to not generate another useless value)

			mMinutes = minutes;
		}

		public int getSeconds() {
			return mMinutes * 60;
		}

		@Override
		public String getSettingsName(Context context) {
			if (mMinutes == 0) {
				return context.getString(R.string.settings_cache_listpreference_no_cache);
			}
			return context.getResources().getQuantityString(R.plurals.settings_cache_listpreference_minutes, mMinutes, mMinutes);
		}
	}

	public CacheHoldTime() {
		super();

		mItems.add(new Item(NO_CACHE));
		mItems.add(new Item(ONE_MINUTE));
		mItems.add(new Item(TWO_MINUTES));
		mItems.add(new Item(FIVE_MINUTES));
		mItems.add(new Item(TEN_MINUTES));
		mItems.add(new Item(THIRY_MINUTES));
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
