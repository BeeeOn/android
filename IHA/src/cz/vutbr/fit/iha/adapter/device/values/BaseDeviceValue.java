package cz.vutbr.fit.iha.adapter.device.values;

import android.content.SharedPreferences;
import cz.vutbr.fit.iha.adapter.device.units.IDeviceUnit;


public abstract class BaseDeviceValue {
	
	public abstract IDeviceUnit getUnit(SharedPreferences prefs);
	
	public abstract String formatValue(SharedPreferences prefs);
	
	public abstract int getIconResource();

	public abstract String getStringValue();
	
	public abstract void setValue(String value);
	
}