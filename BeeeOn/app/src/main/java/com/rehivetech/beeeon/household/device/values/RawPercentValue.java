package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.BaseUnit;
import com.rehivetech.beeeon.household.device.units.PercentUnit;
import com.rehivetech.beeeon.util.Utils;

/**
 * @author martin
 * @since 23/11/2016.
 */

public class RawPercentValue extends BaseValue {

	private static PercentUnit mUnit = new PercentUnit();

	@Override
	public BaseUnit getUnit() {
		return mUnit;
	}

	@Override
	public int getIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_unknown : R.drawable.ic_val_unknown_gray;
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_unknown : R.drawable.ic_val_unknown_gray;
	}

	@Override
	public double getDoubleValue() {
		return Utils.parseDoubleSafely(getRawValue(), 0);
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
