package com.rehivetech.beeeon.util;

import android.content.Context;

import com.rehivetech.beeeon.R;

/**
 * @author David Kozak
 * @since 01.03.2016
 */
public class ActualizationTime extends SettingsItem {
	public static final String PERSISTENCE_ACTUALIZATON_KEY = "pref_actualization";

	public static final int DO_NOT_ACTUALIZE = 0;
	public static final int FIVE_SECONDS = 5;
	public static final int TEN_SECONDS = 10;
	public static final int THIRTY_SECONDS = 30;
	public static final int SIXTY_SECONDS = 60;

	public class Item extends BaseItem{
		private final int mSeconds;

		protected Item(int seconds){
			super(seconds,0);

			mSeconds = seconds;
		}

		public int getSeconds(){
			return mSeconds;
		}

		@Override
		public String getSettingsName(Context context) {
			if(mSeconds == 0) {
				return context.getString(R.string.settings_actualizationtime_listpreference_dont_actualize);
			}
			else
				return context.getResources().getQuantityString(R.plurals.settings_actualization_time_listpreference_seconds,mSeconds,mSeconds);
		}
	}

	public ActualizationTime() {
		super();

		mItems.add(new Item(DO_NOT_ACTUALIZE));
		mItems.add(new Item(FIVE_SECONDS));
		mItems.add(new Item(TEN_SECONDS));
		mItems.add(new Item(THIRTY_SECONDS));
		mItems.add(new Item(SIXTY_SECONDS));
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
