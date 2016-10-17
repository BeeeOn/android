package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.PreferencesHelper;

public class TemperatureUnit extends BaseUnit {

	public TemperatureUnit() {
		super(R.string.pref_unit_temperature_key);

		mItems.add(this.new Item(R.string.pref_unit_temperature_celsius, R.string.unit_temperature_celsius_full, R.string.unit_temperature_celsius));
		mItems.add(this.new Item(R.string.pref_unit_temperature_fahrenheit, R.string.unit_temperature_fahrenheit_full, R.string.unit_temperature_fahrenheit));
		mItems.add(this.new Item(R.string.pref_unit_temperature_kelvin, R.string.unit_temperature_kelvin_full, R.string.unit_temperature_kelvin));
	}

	@Override
	public Item fromSettings(@Nullable SharedPreferences prefs) {
		int itemId = PreferencesHelper.getInt(BeeeOnApplication.getContext(), prefs, mPreferenceKey);
		return mItems.get(itemId);
	}

	@Override
	public double convertValue(Item to, double value) {
		switch (to.getId()) {
			case R.string.pref_unit_temperature_fahrenheit:
				return value * 9 / 5 + 32;
			case R.string.pref_unit_temperature_kelvin:
				return value + 273.15;
			default:
				return value;
		}
	}

	@Override
	public double convertToDefaultValue(Item from, double value) {
		switch (from.getId()) {
			case R.string.pref_unit_temperature_fahrenheit:
				return (value - 32) * 5 / 9;
			case R.string.pref_unit_temperature_kelvin:
				return value - 273.15;
			default:
				return value;
		}
	}
}
