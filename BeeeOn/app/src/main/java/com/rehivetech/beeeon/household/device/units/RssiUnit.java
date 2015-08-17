package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class RssiUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public RssiUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.unit_rssi, R.string.unit_rssi));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_RSSI;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
