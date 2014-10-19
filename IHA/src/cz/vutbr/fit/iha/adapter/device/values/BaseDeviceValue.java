package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.adapter.device.units.BaseUnit;

public abstract class BaseDeviceValue {
	
	public abstract BaseUnit getUnit();
	
	public abstract int getIconResource();

	public abstract String getStringValue();
	
	public abstract void setValue(String value);

	public abstract float getFloatValue();
	
}