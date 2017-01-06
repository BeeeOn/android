package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.R;

import static com.rehivetech.beeeon.household.device.units.BaseUnit.Item.DEFAULT_ID;

public class EmissionUnit extends BaseUnit {

	public EmissionUnit() {
		super(-1);
		mItems.add(new Item(DEFAULT_ID, R.string.unit_emission, R.string.unit_emission));
	}

	@Override
	public Item fromSettings(@Nullable SharedPreferences prefs) {
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
