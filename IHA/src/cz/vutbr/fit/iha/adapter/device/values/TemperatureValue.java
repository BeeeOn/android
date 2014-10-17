package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;


public final class TemperatureValue extends BaseDeviceValue {

	private float mValue = Float.MAX_VALUE;
	// private Temperature mUnit = Temperature.CELSIUS; // FIXME: think this up...
	
	@Override
	public int getUnitStringResource() {
		return R.string.dev_temperature_celsius_unit;
	}

	@Override
	public String getStringValue() {
		return Float.toString(mValue);
	}

	@Override
	public int getRawIntValue() {
		return (int) mValue;
	}

	@Override
	public float getRawFloatValue() {
		return mValue;
	}

	@Override
	public void setValue(String value) {
		mValue = Float.parseFloat(value);
	}

	@Override
	public void setValue(int value) {
		// TODO: check 100
		mValue = value / 100f;
	}
	
}
