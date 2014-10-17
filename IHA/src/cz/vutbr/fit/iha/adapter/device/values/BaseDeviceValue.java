package cz.vutbr.fit.iha.adapter.device.values;

import android.content.Context;

public abstract class BaseDeviceValue {

	/**
	 * Get resource for unit string of this device
	 * 
	 * @return
	 */
	public abstract int getUnitStringResource();
	
	/**
	 * Get value as a string
	 * 
	 * @return value
	 */
	public abstract String getStringValue();

	/**
	 * Get value as a integer if is it possible, zero otherwise
	 * 
	 * @return value
	 */
	public abstract int getRawIntValue();

	/**
	 * Get value as a float if it is possible, zero otherwise
	 * 
	 * @return value
	 */
	public abstract float getRawFloatValue();

	/**
	 * Setting value via string
	 * 
	 * @param value
	 */
	public abstract void setValue(String value);

	/**
	 * Setting value via integer
	 * 
	 * @param value
	 */
	public abstract void setValue(int value);
	
	/**
	 * Get value with unit as string
	 * 
	 * @param context
	 * @return
	 */
	public String getStringValueUnit(Context context) {
		// FIXME: replace with formatting service
		return String.format("%s %s", getStringValue(), getStringUnit(context));
	}

	/**
	 * Get unit as string
	 * 
	 * @param context
	 * @return
	 */
	public String getStringUnit(Context context) {
		// FIXME: replace with formatting service
		int unitRes = getUnitStringResource();
		return unitRes > 0 ? context.getString(unitRes) : "";
	}
	
}
