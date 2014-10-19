package cz.vutbr.fit.iha.adapter.device.units;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

public class TemperatureUnit extends BaseUnit {
	
	public static final int CELSIUS = 0;
	public static final int FAHRENHEIT = 1;
	public static final int KELVIN = 2;
	
	public TemperatureUnit() {		
		super();

		mItems.add(this.new Item(CELSIUS, R.string.dev_temperature_celsius_unit_full, R.string.dev_temperature_celsius_unit));
		mItems.add(this.new Item(FAHRENHEIT, R.string.dev_temperature_fahrenheit_unit_full, R.string.dev_temperature_fahrenheit_unit));
		mItems.add(this.new Item(KELVIN, R.string.dev_temperature_kelvin_unit_full, R.string.dev_temperature_kelvin_unit));
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
	public float convertValue(Item to, float value) {
		switch (to.getId()) {
		case FAHRENHEIT:
			return value * 9 / 5 + 32;
		case KELVIN:
			return value + 273.15f;
		default:
			return value;
		}
	}

}
