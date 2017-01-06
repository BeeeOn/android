package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.R;

import static com.rehivetech.beeeon.household.device.units.BaseUnit.Item.DEFAULT_ID;

public class HumidityUnit extends BaseUnit {

	public HumidityUnit() {
		super(-1);
		mItems.add(new Item(DEFAULT_ID, R.string.unit_humidity, R.string.unit_humidity));
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
