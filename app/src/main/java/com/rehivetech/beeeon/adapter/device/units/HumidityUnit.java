package com.rehivetech.beeeon.adapter.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class HumidityUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public HumidityUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.dev_humidity_unit, R.string.dev_humidity_unit));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_HUMIDITY;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
