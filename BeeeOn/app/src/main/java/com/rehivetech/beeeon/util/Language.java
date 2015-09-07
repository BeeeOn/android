package com.rehivetech.beeeon.util;

import android.content.Context;

import com.rehivetech.beeeon.R;

/**
 * Created by david on 30.8.15.
 */
public class Language extends SettingsItem {

	public static final String PERSISTENCE_PREF_LANGUAGE = "pref_language";

	public static final int FROM_SYSTEM = 0;
	public static final int ENGLISH = 1;
	public static final int CZECH = 2;
	public static final int SLOVAK = 3;

	public Language() {
		super();

		mItems.add(this.new BaseItem(FROM_SYSTEM, 0));
		mItems.add(this.new BaseItem(ENGLISH, 0));
		mItems.add(this.new BaseItem(CZECH, 0));
		mItems.add(this.new BaseItem(SLOVAK, 0));
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
