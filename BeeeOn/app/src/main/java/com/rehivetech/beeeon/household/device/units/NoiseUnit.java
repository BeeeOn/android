package com.rehivetech.beeeon.household.device.units;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.PreferencesHelper;

public class NoiseUnit extends BaseUnit {

	public NoiseUnit() {
		super(R.string.pref_unit_noise_key);

		mItems.add(this.new Item(R.string.pref_unit_noise_decibel, R.string.unit_noise_decibel_full, R.string.unit_noise_decibel));
		mItems.add(this.new Item(R.string.pref_unit_noise_bel, R.string.unit_noise_bel_full, R.string.unit_noise_bel));
		mItems.add(this.new Item(R.string.pref_unit_noise_nepper, R.string.unit_noise_neper_full, R.string.unit_noise_neper));
	}

	@Override
	public Item fromSettings(@Nullable SharedPreferences prefs) {
		int itemId = PreferencesHelper.getInt(BeeeOnApplication.getContext(), prefs, mPreferenceKey);
		return mItems.get(itemId);
	}

	@Override
	public double convertValue(Item to, double value) {
		switch (to.getId()) {
			case R.string.pref_unit_noise_bel:
				return value * 0.1;
			case R.string.pref_unit_noise_nepper:
				return value / (20 * Math.log10(Math.E));
			default:
				return value;
		}
	}
}
