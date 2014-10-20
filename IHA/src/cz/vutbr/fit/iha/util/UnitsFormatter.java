package cz.vutbr.fit.iha.util;

import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.adapter.device.units.BaseUnit;
import cz.vutbr.fit.iha.adapter.device.values.BaseDeviceValue;
import cz.vutbr.fit.iha.adapter.device.values.BaseEnumValue;

public class UnitsFormatter {
	
	private SharedPreferences mPrefs;
	private Context mContext;
	
	public UnitsFormatter(SharedPreferences prefs, Context context) {
		mPrefs = prefs;
		mContext = context;
	}
	
	public String getStringValue(BaseDeviceValue item, float value) {
		// FIXME: Fix BaseEnumValue when they will be supported in graphs
		//if (item instanceof BaseEnumValue) { ... }
		
		BaseUnit.Item to = item.getUnit().fromSettings(mPrefs);
		float f = item.getUnit().convertValue(to, value);
		return Utils.formatFloat(f);
	}
	
	public String getStringValue(BaseDeviceValue item) {
		if (item instanceof BaseEnumValue) {
			int resId = ((BaseEnumValue) item).getStateStringResource();
			return mContext.getString(resId);
		}
		
		return getStringValue(item, item.getFloatValue());
	}
	
	public String getStringUnit(BaseDeviceValue item) {
		return item.getUnit().fromSettings(mPrefs).getStringUnit(mContext);
	}
	
	public String getStringValueUnit(BaseDeviceValue item) {
		String value = getStringValue(item);
		String unit = getStringUnit(item);
		
		if (unit.isEmpty())
			return value;
		
		return String.format("%s %s", value, unit);
	}
		
}
