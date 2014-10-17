package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;


public final class UnknownValue extends BaseDeviceValue {

	private String mValue = "";

	@Override
	public void setValue(String value) {
		mValue = value;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.dev_unknown_unit;
	}

	@Override
	public int getRawIntValue() {
		return Integer.MAX_VALUE;
	}

	@Override
	public float getRawFloatValue() {
		return Float.NaN;
	}

	@Override
	public void setValue(int value) {
		mValue = Integer.toString(value);
	}

	@Override
	public String getStringValue() {
		return mValue;
	}
	
}
