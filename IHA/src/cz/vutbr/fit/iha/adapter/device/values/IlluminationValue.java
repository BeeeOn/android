package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;

public final class IlluminationValue extends BaseDeviceValue {

	private float mValue = Float.MAX_VALUE;

	@Override
	public void setValue(String value) {
		mValue = Float.parseFloat(value);
	}
	
	@Override
	public String getStringValue() {
		return String.valueOf(mValue);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_illumination;
	}
	
	@Override
	public int getUnitResource() {
		return R.string.dev_illumination_unit;
	}
	
	public float getValue() {
		return mValue;
	}
	
}
