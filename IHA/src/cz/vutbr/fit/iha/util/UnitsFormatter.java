package cz.vutbr.fit.iha.util;

import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.adapter.device.units.IDeviceUnit;
import cz.vutbr.fit.iha.adapter.device.values.BaseDeviceValue;

public class UnitsFormatter {
	
	private SharedPreferences mPrefs;
	private Context mContext;
	
	public UnitsFormatter(SharedPreferences prefs, Context context) {
		mPrefs = prefs;
		mContext = context;
	}
	
	public String getStringValue(BaseDeviceValue value) {
		return value.formatValue(mPrefs);
	}
	
	public String getStringUnit(BaseDeviceValue value) {
		return value.getUnit(mPrefs).getUnit(mContext);
	}
	
	public String getStringValueUnit(BaseDeviceValue item) {
		String value = getStringValue(item);
		String unit = getStringUnit(item);
		
		if (unit.isEmpty())
			return value;
			
		return String.format("%s %s", value, unit);
	}

	public String formatRawValueUnit(float valueF, BaseDeviceValue item) {
		IDeviceUnit data = item.getUnit(mPrefs);
		
		String value = data.formatValue(valueF);
		String unit = data.getUnit(mContext);

		if (unit.isEmpty())
			return value;
		
		return String.format("%s %s", value, unit);
	}
		
}
