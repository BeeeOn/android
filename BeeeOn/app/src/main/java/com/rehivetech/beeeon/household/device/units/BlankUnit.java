package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.R;

public class BlankUnit extends BaseUnit {

	public BlankUnit() {
		super(-1);
		mItems.add(new Item(Item.DEFAULT_ID, R.string.empty, R.string.empty));
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
