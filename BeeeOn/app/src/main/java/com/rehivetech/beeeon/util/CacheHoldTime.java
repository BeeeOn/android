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


	//TODO: is it necessary to have items class in every SettingsItem class?
	public class Item extends SettingsItem.BaseItem {
		private final int mResName;

		protected Item(int id, int resName) {
			super(id);

			this.mResName = resName;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getSettingsName(Context context) {
			return context.getString(mResName);
		}
	}

	public CacheHoldTime() {
		super();

		mItems.add(new Item(DO_NOT_ACTUALIZE, 0));
		mItems.add(new Item(ONE_MINUTE,0));
		mItems.add(new Item(TWO_MINUTES,0));
		mItems.add(new Item(FIVE_MINUTES,0));
		mItems.add(new Item(TEN_MINUTES,0));
		mItems.add(new Item(THIRY_MINUTES,0));
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
