package cz.vutbr.fit.iha.adapter.device.values;


public abstract class BaseDeviceValue {
	
	public abstract int getIconResource();
	
	@Deprecated
	public abstract int getUnitResource();
	
	public abstract String getStringValue();
	
	public abstract void setValue(String value);
	
}