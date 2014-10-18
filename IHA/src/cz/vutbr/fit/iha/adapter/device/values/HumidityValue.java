package cz.vutbr.fit.iha.adapter.device.values;

import android.content.SharedPreferences;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.HumidityUnit;
import cz.vutbr.fit.iha.util.Utils;

public final class HumidityValue extends BaseDeviceValue {

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
		return R.drawable.dev_humidity;
	}
	
	@Override
	public HumidityUnit getUnit(SharedPreferences prefs) {
		return HumidityUnit.DEFAULT;
	}
	
	@Override
	public String formatValue(SharedPreferences prefs) {
		float value = getUnit(prefs).convertValue(mValue);
		return Utils.formatFloat(value);
	}
	
	public float getValue() {
		return mValue;
	}
	
}
