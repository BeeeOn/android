package com.rehivetech.beeeon.adapter.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class EmissionUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public EmissionUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.dev_emission_unit, R.string.dev_emission_unit));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_EMISSION;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
