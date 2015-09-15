package com.rehivetech.beeeon.util;

import com.rehivetech.beeeon.R;

import java.util.Locale;

/**
 * Created by david on 30.8.15.
 */
public class Language extends SettingsItem {

	public static final String PERSISTENCE_PREF_LANGUAGE = "pref_language";

	public static final int FROM_SYSTEM = 0;
	public static final int ENGLISH = 1;
	public static final int CZECH = 2;
	public static final int SLOVAK = 3;

	public class Item extends BaseItem {
		private final String mCode;

		protected Item(int id, int resName, String code) {
			super(id, resName);

			this.mCode = code;
		}

		public String getCode() {
			if (mCode == null) {
				return Locale.getDefault().toString();
			}
			return mCode;
		}
	}

	public Language() {
		super();

		mItems.add(this.new Item(FROM_SYSTEM, R.string.settings_language_listpreference_default, null));
		mItems.add(this.new Item(ENGLISH, R.string.settings_language_listpreference_english, "en"));
		mItems.add(this.new Item(CZECH, R.string.settings_language_listpreference_czech, "cs"));
		mItems.add(this.new Item(SLOVAK, R.string.settings_language_listpreference_slovak, "sk"));
	}

	@Override
	public int getDefaultId() {
		return FROM_SYSTEM;
	}

	@Override
	public String getPersistenceKey() {
		return PERSISTENCE_PREF_LANGUAGE;
	}
}
