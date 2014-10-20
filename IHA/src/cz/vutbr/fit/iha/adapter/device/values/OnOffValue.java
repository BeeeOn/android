package cz.vutbr.fit.iha.adapter.device.values;

import android.graphics.Color;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.BlankUnit;

public class OnOffValue extends BaseEnumValue {

	public static final String SWITCH_ON = "ON";
	public static final String SWITCH_OFF = "OFF";

	private String mValue = "";

	private static BlankUnit mUnit = new BlankUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		setActive(value.equalsIgnoreCase(SWITCH_ON));
	}

	@Override
	public int getIconResource() {
		return isActive() ? R.drawable.dev_switch_on : R.drawable.dev_switch_off;
	}

	@Override
	public BlankUnit getUnit() {
		return mUnit;
	}

	@Override
	public float getFloatValue() {
		return Float.NaN;
	}

	private boolean isActive() {
		return mValue.equals(SWITCH_ON);
	}

	private void setActive(boolean on) {
		mValue = (on ? SWITCH_ON : SWITCH_OFF);
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
		return isActive() ? R.string.dev_switch_value_on : R.string.dev_switch_value_off;
	}

}
