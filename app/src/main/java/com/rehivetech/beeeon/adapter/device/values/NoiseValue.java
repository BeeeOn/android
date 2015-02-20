package com.rehivetech.beeeon.adapter.device.values;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.units.NoiseUnit;

public final class NoiseValue extends BaseValue {

	private double mValue = Double.NaN;

	private static NoiseUnit mUnit = new NoiseUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_noise;
	}

	@Override
	public NoiseUnit getUnit() {
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
