package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.HumidityUnit;

public final class HumidityValue extends BaseValue {

	private double mValue = Double.NaN;

	private static HumidityUnit mUnit = new HumidityUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_humidity : R.drawable.ic_val_humidity_gray;
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		// FIXME: Use real resource when we will have real actor icon
		return getIconResource(type);
	}

	@Override
	public HumidityUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

}
