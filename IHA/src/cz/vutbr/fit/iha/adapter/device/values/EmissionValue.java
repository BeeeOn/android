package cz.vutbr.fit.iha.adapter.device.values;

import android.content.SharedPreferences;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.EmissionUnit;
import cz.vutbr.fit.iha.util.Utils;

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
	public EmissionUnit getUnit(SharedPreferences prefs) {
		return EmissionUnit.DEFAULT;
	}
	
	@Override
	public String formatValue(SharedPreferences prefs) {
		float value = getUnit(prefs).convertValue(mValue);
		return Utils.formatFloat(value);
	}
	
	public int getValue() {
		return mValue;
	}
	
}
