package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class IlluminationUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public IlluminationUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.unit_illumination, R.string.unit_illumination));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_ILLUMINATION;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
