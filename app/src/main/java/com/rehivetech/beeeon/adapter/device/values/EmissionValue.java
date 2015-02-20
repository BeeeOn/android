package com.rehivetech.beeeon.adapter.device.values;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.units.EmissionUnit;

public final class EmissionValue extends BaseValue {

	private double mValue = Double.NaN;

	private static EmissionUnit mUnit = new EmissionUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_emission;
	}

	@Override
	public EmissionUnit getUnit() {
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
