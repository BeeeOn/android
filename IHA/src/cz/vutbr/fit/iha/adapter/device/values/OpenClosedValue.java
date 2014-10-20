package cz.vutbr.fit.iha.adapter.device.values;

import android.graphics.Color;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.BlankUnit;

public class OpenClosedValue extends BaseEnumValue {

	private String mValue = "";

	private static BlankUnit mUnit = new BlankUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		setActive(value.equalsIgnoreCase(STATE_OPEN));
	}

	@Override
	public BlankUnit getUnit() {
		return mUnit;
	}

	public static final String STATE_OPEN = "OPEN";
	public static final String STATE_CLOSED = "CLOSED";

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

	@Override
	public int getIconResource() {
		return isActive() ? R.drawable.dev_state_open : R.drawable.dev_state_closed;
	}

	@Override
	public float getFloatValue() {
		return Float.NaN;
	}

}
