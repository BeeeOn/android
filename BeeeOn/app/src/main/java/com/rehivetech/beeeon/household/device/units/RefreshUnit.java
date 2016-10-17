package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.R;

import static com.rehivetech.beeeon.household.device.units.BaseUnit.Item.DEFAULT_ID;

public class RefreshUnit extends BaseUnit {

	public RefreshUnit() {
		super(-1);

		mItems.add(new Item(DEFAULT_ID, R.string.unit_refresh, R.string.unit_refresh));
	}

	@Override
	public Item fromSettings(@Nullable SharedPreferences prefs) {
		return mItems.get(0);
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}
}
