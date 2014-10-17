package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;


public final class EmissionValue extends BaseDeviceValue {

	private int mValue = Integer.MAX_VALUE;
	
	@Override
	public int getUnitStringResource() {
		return R.string.dev_emission_unit;
	}

	@Override
	public String getStringValue() {
		return Integer.toString(mValue);
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
	public void setValue(int value) {
		mValue = value;
	}
	
}
