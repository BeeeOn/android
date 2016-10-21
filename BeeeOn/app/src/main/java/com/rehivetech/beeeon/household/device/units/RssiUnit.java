package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.R;

import static com.rehivetech.beeeon.household.device.units.BaseUnit.Item.DEFAULT_ID;

public class RssiUnit extends BaseUnit {

	public RssiUnit() {
		super(-1);
		mItems.add(this.new Item(DEFAULT_ID, R.string.unit_rssi, R.string.unit_rssi));
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
