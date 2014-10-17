package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;

public class OnOffValue extends BaseEnumValue {

	private String mValue = "";
	
	@Override
	public void setValue(String value) {
		setActive(value.equalsIgnoreCase(SWITCH_ON));
	}

	public static final String SWITCH_ON = "ON";
	public static final String SWITCH_OFF = "OFF";

	/**
	 * This method shouldn't be used for this type of device, use getStateStringResource() instead
	 */
	@Override
	public String getStringValue() {
		return mValue;
	}

	@Override
	public int getUnitStringResource() {
		return 0;
	}

	@Override
	public int getRawIntValue() {
		return Integer.MAX_VALUE;
	}

	@Override
	public float getRawFloatValue() {
		return Float.NaN;
	}

	@Override
	public void setValue(int value) {
		mValue = (value != 0 ? SWITCH_ON : SWITCH_OFF);
	}

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

}
