package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.TemperatureUnit;

public final class TemperatureValue extends BaseDeviceValue {

	private float mValue = Float.MAX_VALUE;

	private static TemperatureUnit mUnit = new TemperatureUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Float.parseFloat(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_temperature;
	}

	public float getValue() {
		return mValue;
	}

	@Override
	public float getFloatValue() {
		return mValue;
	}

	@Override
	public TemperatureUnit getUnit() {
		return mUnit;
	}
	
}
