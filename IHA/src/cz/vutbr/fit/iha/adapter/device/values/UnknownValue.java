package cz.vutbr.fit.iha.adapter.device.values;

import android.content.SharedPreferences;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.UnknownUnit;

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
	public UnknownUnit getUnit(SharedPreferences prefs) {
		return UnknownUnit.DEFAULT;
	}
	
	public String getValue() {
		return mValue;
	}
	
	@Override
	public String formatValue(SharedPreferences prefs) {
		return mValue;
	}
	
}
