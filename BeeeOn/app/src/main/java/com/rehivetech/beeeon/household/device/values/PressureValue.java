package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.PressureUnit;
import com.rehivetech.beeeon.util.Utils;

public final class PressureValue extends BaseValue {

	private double mValue = Double.NaN;

	private static PressureUnit mUnit = new PressureUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Utils.parseDoubleSafely(value, Double.NaN);
	}

	@Override
	public int getIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_pressure : R.drawable.ic_val_pressure_gray;
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		// FIXME: Use real resource when we will have real actor icon
		return getIconResource(type);
	}

	@Override
	public PressureUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

	// NOTE: Guessed from https://en.wikipedia.org/wiki/Atmospheric_pressure

	@Override
	public double getSaneMinimum() {
		return 970;
	}

	@Override
	public double getSaneMaximum() {
		return 1050;
	}

	@Override
	public double getSaneStep() {
		return 1.3;
	}
}
