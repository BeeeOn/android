package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;

public final class EmissionValue extends BaseDeviceValue {

	private int mValue = Integer.MAX_VALUE;
	
	@Override
	public void setValue(String value) {
		mValue = Integer.parseInt(value);
	}
	
	@Override
	public String getStringValue() {
		return String.valueOf(mValue);
	}
	
	@Override
	public int getIconResource() {
		return R.drawable.dev_emission;
	}
	
	@Override
	public int getUnitResource() {
		return R.string.dev_emission_unit;
	}
	
	public int getValue() {
		return mValue;
	}
	
}
