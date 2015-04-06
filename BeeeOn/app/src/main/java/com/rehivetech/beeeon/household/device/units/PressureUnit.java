package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class PressureUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public PressureUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.dev_pressure_unit, R.string.dev_pressure_unit));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_PRESSURE;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
