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


	//TODO: is it necessary to have items class in every SettingsItem class?
	public class Item extends BaseItem{
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

	public ActualizationTime() {
		super();

		mItems.add(new Item(DO_NOT_ACTUALIZE, 0));
		mItems.add(new Item(FIVE_SECONDS,0));
		mItems.add(new Item(TEN_SECONDS,0));
		mItems.add(new Item(THIRTY_SECONDS,0));
		mItems.add(new Item(SIXTY_SECONDS,0));
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
