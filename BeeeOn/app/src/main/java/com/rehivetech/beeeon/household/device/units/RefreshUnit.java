package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class RefreshUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public RefreshUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.unit_refresh, R.string.unit_refresh));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_REFRESH;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
