package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.UnknownUnit;

public final class UnknownValue extends BaseValue {

	private String mValue = "";

	private static UnknownUnit mUnit = new UnknownUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = value;
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_unknown;
	}

	@Override
	public UnknownUnit getUnit() {
		return mUnit;
	}

	public String getValue() {
		return mValue;
	}

	@Override
	public double getDoubleValue() {
		return Double.NaN;
	}

}
