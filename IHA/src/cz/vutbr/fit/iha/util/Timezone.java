package cz.vutbr.fit.iha.util;

import android.content.Context;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

public class Timezone extends SettingsItem {

	public static final int ACTUAL = 0;
	public static final int ADAPTER = 1;

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

		mItems.add(this.new Item(ACTUAL, R.string.actual_timezone));
		mItems.add(this.new Item(ADAPTER, R.string.adapter_timezone));
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
