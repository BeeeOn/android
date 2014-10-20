package cz.vutbr.fit.iha.util;

import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.adapter.device.units.TemperatureUnit;
import cz.vutbr.fit.iha.adapter.device.values.BaseDeviceValue;
import cz.vutbr.fit.iha.adapter.device.values.TemperatureValue;

public class UnitsFormatter {
	
	private SharedPreferences mPrefs;
	private Context mContext;
	
	public UnitsFormatter(SharedPreferences prefs, Context context) {
		mPrefs = prefs;
		mContext = context;
	}
	
	public String getStringValue(BaseDeviceValue value) {
		if (value instanceof TemperatureValue) {
			float f = ((TemperatureValue)value).getValue();
			f = TemperatureUnit.fromSettings(mPrefs).convertValue(f);
			return Utils.formatFloat(f);
		}
		
		// FIXME: Dummy version of getting value (no settings/formatting)
		return value.getStringValue();
	}
	
	public String getStringUnit(BaseDeviceValue value) {
		if (value instanceof TemperatureValue) {
			return TemperatureUnit.fromSettings(mPrefs).getUnit(mContext);
		}
		
		// FIXME: Dummy version of getting unit (no settings/formatting)
		return mContext.getString(value.getUnitResource());
	}
	
	public String getStringValueUnit(BaseDeviceValue item) {
		String value = getStringValue(item);
		String unit = getStringUnit(item);
		
		if (unit.isEmpty())
			return value;
			
		return String.format("%s %s", value, unit);
	}

	public String formatRawValueUnit(float valueF, BaseDeviceValue item) {
		// Replace that legacy value/unit with converted one
		if (item instanceof TemperatureValue) {
			valueF = TemperatureUnit.fromSettings(mPrefs).convertValue(valueF);
		}
		
		String value = Utils.formatFloat(valueF);
		String unit = getStringUnit(item);

		if (unit.isEmpty())
			return value;
		
		return String.format("%s %s", value, unit);
	}
	
	
		
}
