package com.rehivetech.beeeon.util;

import android.content.Context;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class Timezone extends SettingsItem {

	public static final int ACTUAL = 0;
	public static final int GATE = 1;

	public class Item extends BaseItem {
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

	public Timezone() {
		super();

		mItems.add(this.new Item(ACTUAL, R.string.util_timezone_actual_timezone));
		mItems.add(this.new Item(GATE, R.string.util_timezone_gate_timezone));
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_TIMEZONE;
	}

	@Override
	public int getDefaultId() {
		return ACTUAL;
	}

}
