package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.PressureUnit;

public final class PressureValue extends BaseValue {

	private double mValue = Double.NaN;

	private static PressureUnit mUnit = new PressureUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_pressure;
	}

	@Override
	public int getActorIconResource() {
		// FIXME: Use real resource when we will have real actor icon
		return getIconResource();
	}

	@Override
	public PressureUnit getUnit() {
		return mUnit;
	}

	public double getValue() {
		return mValue;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

}
