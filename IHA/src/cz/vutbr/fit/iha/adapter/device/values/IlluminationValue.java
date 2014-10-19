package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.IlluminationUnit;

public final class IlluminationValue extends BaseDeviceValue {

	private float mValue = Float.MAX_VALUE;

	private static IlluminationUnit mUnit = new IlluminationUnit();
	
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
	public IlluminationUnit getUnit() {
		return mUnit;
	}
	
	public float getValue() {
		return mValue;
	}
	
	@Override
	public float getFloatValue() {
		return mValue;
	}

}
