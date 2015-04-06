package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.R;

public class UnknownUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public UnknownUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.dev_unknown_unit, R.string.dev_unknown_unit));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return "";
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
