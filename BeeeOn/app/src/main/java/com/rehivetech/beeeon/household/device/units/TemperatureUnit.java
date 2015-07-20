package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class TemperatureUnit extends BaseUnit {

	public static final int CELSIUS = 0;
	public static final int FAHRENHEIT = 1;
	public static final int KELVIN = 2;

	public TemperatureUnit() {
		super();

		mItems.add(this.new Item(CELSIUS, R.string.unit_temperature_celsius_full, R.string.unit_temperature_celsius));
		mItems.add(this.new Item(FAHRENHEIT, R.string.unit_temperature_fahrenheit_full, R.string.unit_temperature_fahrenheit));
		mItems.add(this.new Item(KELVIN, R.string.unit_temperature_kelvin_full, R.string.unit_temperature_kelvin));
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_TEMPERATURE;
	}

	@Override
	public int getDefaultId() {
		return CELSIUS;
	}

	@Override
	public double convertValue(Item to, double value) {
		switch (to.getId()) {
			case FAHRENHEIT:
				return value * 9 / 5 + 32;
			case KELVIN:
				return value + 273.15;
			default:
				return value;
		}
	}

}
