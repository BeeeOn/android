package cz.vutbr.fit.iha.adapter.device.values;

import android.graphics.Color;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.BlankUnit;

public class OpenClosedValue extends BaseEnumValue {

	public static final String STATE_OPEN = "OPEN";
	public static final String STATE_CLOSED = "CLOSED";
	
	private String mValue = "";

	private static BlankUnit mUnit = new BlankUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		setActive(value.equalsIgnoreCase(STATE_OPEN));
	}
	
	@Override
	public int getIconResource() {
		return isActive() ? R.drawable.dev_state_open : R.drawable.dev_state_closed;
	}

	@Override
	public BlankUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return Double.NaN;
	}

	private boolean isActive() {
		return mValue.equals(STATE_OPEN);
	}

	private void setActive(boolean open) {
		mValue = (open ? STATE_OPEN : STATE_CLOSED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColorByState() {
		return isActive() ? Color.GREEN : Color.RED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStateStringResource() {
		return isActive() ? R.string.dev_state_value_open : R.string.dev_state_value_closed;
	}

}
