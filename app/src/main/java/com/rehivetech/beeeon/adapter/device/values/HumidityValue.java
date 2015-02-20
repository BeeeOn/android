package com.rehivetech.beeeon.adapter.device.values;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.units.HumidityUnit;

public final class HumidityValue extends BaseValue {

	private double mValue = Double.NaN;

	private static HumidityUnit mUnit = new HumidityUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_humidity;
	}

	@Override
	public HumidityUnit getUnit() {
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
