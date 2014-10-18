package cz.vutbr.fit.iha.adapter.device.values;

import android.content.SharedPreferences;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.PressureUnit;
import cz.vutbr.fit.iha.util.Utils;

public final class PressureValue extends BaseDeviceValue {

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
		return R.drawable.dev_pressure;
	}
	
	@Override
	public PressureUnit getUnit(SharedPreferences prefs) {
		return PressureUnit.DEFAULT;
	}
	
	public int getValue() {
		return mValue;
	}
	
	@Override
	public String formatValue(SharedPreferences prefs) {
		float value = getUnit(prefs).convertValue(mValue);
		return Utils.formatFloat(value);
	}
	
}
