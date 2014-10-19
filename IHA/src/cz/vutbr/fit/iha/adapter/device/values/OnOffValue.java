package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.BlankUnit;

public class OnOffValue extends BaseEnumValue {

	private String mValue = "";
	
	private static BlankUnit mUnit = new BlankUnit();
	
	@Override
	public void setValue(String value) {
		setActive(value.equalsIgnoreCase(SWITCH_ON));
	}
	
	@Override
	public String getStringValue() {
		return mValue;
	}
	
	@Override
	public BlankUnit getUnit() {
		return mUnit;
	}
		
	public static final String SWITCH_ON = "ON";
	public static final String SWITCH_OFF = "OFF";

	/**
	 * Return info about active state of device
	 * 
	 * @return boolean representing active state
	 */
	public boolean isActive() {
		return mValue.equals(SWITCH_ON);
	}

	public void setActive(boolean on) {
		mValue = (on ? SWITCH_ON : SWITCH_OFF);
	}

	/**
	 * Get resource for human readable string representing state of this device
	 * 
	 * @return int
	 */
	public int getStateStringResource() {
		return isActive() ? R.string.dev_switch_value_on : R.string.dev_switch_value_off;
	}
	
	@Override
	public int getIconResource() {
		return isActive() ? R.drawable.dev_switch_on : R.drawable.dev_switch_off;
	}

	@Override
	public float getFloatValue() {
		return Float.NaN;
	}
	
}
