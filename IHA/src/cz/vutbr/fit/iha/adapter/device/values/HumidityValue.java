package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.HumidityUnit;

public final class HumidityValue extends BaseDeviceValue {

	private float mValue = Float.MAX_VALUE;

	private static HumidityUnit mUnit = new HumidityUnit();
	
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
		return R.drawable.dev_humidity;
	}
	
	@Override
	public HumidityUnit getUnit() {
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
