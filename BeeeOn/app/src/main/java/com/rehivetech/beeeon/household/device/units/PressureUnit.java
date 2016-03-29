package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class PressureUnit extends BaseUnit {

	public static final int HPA = 0;
	public static final int BAR = 1;

	public PressureUnit() {
		super();

		mItems.add(this.new Item(HPA, R.string.unit_pressure, R.string.unit_pressure));
		mItems.add(this.new Item(BAR, R.string.unit_pressure_bar_full, R.string.unit_pressure_bar));
	}

	@Override
	public int getDefaultId() {
		return HPA;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_PRESSURE;
	}

	@Override
	public double convertValue(Item to, double value) {
		switch (to.getId()) {
			case BAR:
				return value * 0.001;
			default:
				return value;
		}


	}

}
