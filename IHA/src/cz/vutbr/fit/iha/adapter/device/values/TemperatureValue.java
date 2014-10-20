package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;

public final class TemperatureValue extends BaseDeviceValue {

	private float mValue = Float.MAX_VALUE;
	
	@Override
	public void setValue(String value) {
		mValue = Float.parseFloat(value);
	}

	public void setValue(int value) {
		// TODO: check 100
		mValue = value / 100f;
	}
	
	@Override
	public String getStringValue() {
		return String.valueOf(mValue);
	}
	
	@Override
	public int getUnitResource() {
		return R.string.dev_temperature_celsius_unit; // FIXME: remove this
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_temperature;
	}

	public float getValue() {
		return mValue;
	}
	
}
