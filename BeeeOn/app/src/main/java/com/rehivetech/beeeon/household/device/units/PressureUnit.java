package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.PreferencesHelper;

public class PressureUnit extends BaseUnit {

	public PressureUnit() {
		super(R.string.pref_unit_pressure_key);

		mItems.add(this.new Item(R.string.pref_unit_pressure, R.string.unit_pressure, R.string.unit_pressure));
		mItems.add(this.new Item(R.string.pref_unit_pressure_bar, R.string.unit_pressure_bar_full, R.string.unit_pressure_bar));
	}

	@Override
	public Item fromSettings(@Nullable SharedPreferences prefs) {
		int itemId = PreferencesHelper.getInt(BeeeOnApplication.getContext(), prefs, mPreferenceKey);
		return mItems.get(itemId);
	}

	@Override
	public double convertValue(Item to, double value) {
		switch (to.getId()) {
			case R.string.pref_unit_pressure_bar:
				return value * 0.001;
			default:
				return value;
		}
	}
}
