package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;


public final class PressureValue extends BaseDeviceValue {

	private int mValue = Integer.MAX_VALUE;
	
	@Override
	public void setValue(int value) {
		mValue = value;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.dev_pressure_unit;
	}

	@Override
	public int getRawIntValue() {
		return mValue;
	}

	@Override
	public float getRawFloatValue() {
		return mValue;
	}

	@Override
	public void setValue(String value) {
		mValue = Integer.parseInt(value);
	}

	@Override
	public String getStringValue() {
		return Integer.toString(mValue);
	}

}
