package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.TemperatureUnit;

public final class TemperatureValue extends BaseValue {

	private double mValue = Double.MAX_VALUE;

	private static TemperatureUnit mUnit = new TemperatureUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_temperature;
	}

	public double getValue() {
		return mValue;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

	@Override
	public TemperatureUnit getUnit() {
		return mUnit;
	}
	
}
