package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.EmissionUnit;
import com.rehivetech.beeeon.util.Utils;

public final class EmissionValue extends BaseValue {

	private double mValue = Double.NaN;

	private static EmissionUnit mUnit = new EmissionUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Utils.parseDoubleSafely(value, Double.NaN);
	}

	@Override
	public int getIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_emission : R.drawable.ic_val_emission_gray;
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		// FIXME: Use real resource when we will have real actor icon
		return getIconResource(type);
	}

	@Override
	public EmissionUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

	// NOTE: Guessed from http://www.theben.de/en/CO2-sensors

	@Override
	public double getSaneMinimum() {
		return 350;
	}

	@Override
	public double getSaneMaximum() {
		return 4000;
	}

	@Override
	public double getSaneStep() {
		return 10;
	}
}
