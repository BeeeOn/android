package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.NoiseUnit;

public final class NoiseValue extends BaseValue {

	private double mValue = Double.NaN;

	private static NoiseUnit mUnit = new NoiseUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_noise : R.drawable.ic_val_noise_gray;
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		// FIXME: Use real resource when we will have real actor icon
		return getIconResource(type);
	}

	@Override
	public NoiseUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

	// NOTE: Guessed from http://www.gcaudio.com/resources/howtos/loudness.html

	@Override
	public double getSaneMinimum() {
		return 0;
	}

	@Override
	public double getSaneMaximum() {
		return 90;
	}

	@Override
	public double getSaneStep() {
		return 2;
	}
}
