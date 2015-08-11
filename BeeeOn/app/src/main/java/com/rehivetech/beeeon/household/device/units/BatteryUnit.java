package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class BatteryUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public BatteryUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.unit_battery, R.string.unit_battery));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_BATTERY;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
