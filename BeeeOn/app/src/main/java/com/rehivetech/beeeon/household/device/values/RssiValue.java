package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.RssiUnit;
import com.rehivetech.beeeon.util.Utils;

public final class RssiValue extends BaseValue {

	private double mValue = Double.NaN;

	private static RssiUnit mUnit = new RssiUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Utils.parseDoubleSafely(value, Double.NaN);
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
	public RssiUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

	@Override
	public double getSaneMinimum() {
		return 0;
	}

	@Override
	public double getSaneMaximum() {
		return 100;
	}

	@Override
	public double getSaneStep() {
		return 1;
	}
}
