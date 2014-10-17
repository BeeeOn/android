package cz.vutbr.fit.iha.adapter.device.values;

import android.graphics.Color;
import cz.vutbr.fit.iha.R;

public class OpenClosedValue extends BaseEnumValue {

	private String mValue = "";
	
	@Override
	public void setValue(String value) {
		setActive(value.equalsIgnoreCase(STATE_OPEN));
	}

	public static final String STATE_OPEN = "OPEN";
	public static final String STATE_CLOSED = "CLOSED";

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
		mValue = (value != 0 ? STATE_OPEN : STATE_CLOSED);
	}

	/**
	 * This method shouldn't be used for this type of device, use getStateStringResource() instead
	 */
	@Override
	public String getStringValue() {
		return mValue;
	}

	/**
	 * Return info about active state of device
	 * 
	 * @return boolean representing active state
	 */
	public boolean isActive() {
		return mValue.equals(STATE_OPEN);
	}

	public void setActive(boolean open) {
		mValue = (open ? STATE_OPEN : STATE_CLOSED);
	}

	/**
	 * Return color by state
	 * 
	 * @return int representing color GREEN or RED
	 */
	public int getColorByState() {
		return isActive() ? Color.GREEN : Color.RED;
	}

	/**
	 * Get resource for human readable string representing state of this device
	 * 
	 * @return int
	 */
	public int getStateStringResource() {
		return isActive() ? R.string.dev_state_value_open : R.string.dev_state_value_closed;
	}

}
