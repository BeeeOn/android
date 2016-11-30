package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;

import com.rehivetech.beeeon.R;

/**
 * @author martin
 * @since 23/11/2016.
 */

public class PercentUnit extends BaseUnit {

	public PercentUnit() {
		super(-1);
		mItems.add(new Item(Item.DEFAULT_ID, R.string.unit_percent, R.string.unit_percent));
	}

	@Override
	public Item fromSettings(SharedPreferences prefs) {
		return mItems.get(0);
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

	@Override
	public double convertToDefaultValue(Item from, double value) {
		return value;
	}
}
