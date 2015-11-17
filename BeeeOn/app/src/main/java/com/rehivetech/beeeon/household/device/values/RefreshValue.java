package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.units.RefreshUnit;

public final class RefreshValue extends BaseValue {

	private double mValue = Double.NaN;

	private static RefreshUnit mUnit = new RefreshUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource(IconResourceType type) {
		// FIXME: Use correct icon
		return type == IconResourceType.WHITE ? R.drawable.ic_val_unknown : R.drawable.ic_val_unknown_gray;
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		// FIXME: Use real resource when we will have real actor icon
		return getIconResource(type);
	}

	@Override
	public RefreshUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

	@Override
	public double getSaneMinimum() {
		return RefreshInterval.SEC_1.getInterval();
	}

	@Override
	public double getSaneMaximum() {
		return RefreshInterval.HOUR_24.getInterval();
	}

	@Override
	public double getSaneStep() {
		return 30;
	}
}
