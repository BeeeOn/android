package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.TemperatureUnit;
import com.rehivetech.beeeon.util.Utils;

public final class TemperatureValue extends BaseValue {

	private double mValue = Double.NaN;

	private static TemperatureUnit mUnit = new TemperatureUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Utils.parseDoubleSafely(value, Double.NaN);
	}

	@Override
	public int getIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_temperature : R.drawable.ic_val_temperature_gray;
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_temperature_actor : R.drawable.ic_val_temperature_actor_gray;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

	@Override
	public TemperatureUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getSaneMinimum() {
		return 10;
	}

	@Override
	public double getSaneMaximum() {
		return 40;
	}

	@Override
	public double getSaneStep() {
		return 0.1;
	}
}
