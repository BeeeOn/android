package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;

public final class UnknownValue extends BaseDeviceValue {

	private String mValue = "";

	@Override
	public void setValue(String value) {
		mValue = value;
	}
	
	@Override
	public String getStringValue() {
		return mValue;
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_unknown;
	}
	
	@Override
	public int getUnitResource() {
		return R.string.dev_unknown_unit;
	}
	
	public String getValue() {
		return mValue;
	}
	
}
