package cz.vutbr.fit.iha.adapter.device.values;

import android.content.SharedPreferences;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.TemperatureUnit;
import cz.vutbr.fit.iha.util.Utils;

public final class TemperatureValue extends BaseDeviceValue {

	private float mValue = Float.MAX_VALUE;
	
	@Override
	public void setValue(String value) {
		mValue = Float.parseFloat(value);
	}

	public void setValue(int value) {
		// TODO: check 100
		mValue = value / 100f;
	}
	
	@Override
	public String getStringValue() {
		return String.valueOf(mValue);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_temperature;
	}

	public float getValue() {
		return mValue;
	}

	@Override
	public TemperatureUnit getUnit(SharedPreferences prefs) {
		return TemperatureUnit.fromSettings(prefs);
	}
	
	@Override
	public String formatValue(SharedPreferences prefs) {
		float value = getUnit(prefs).convertValue(mValue);
		return Utils.formatFloat(value);
	}
	
}
