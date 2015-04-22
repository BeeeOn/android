package com.rehivetech.beeeon.household.device.values;

public abstract class BooleanValue extends BaseEnumValue {

	public static final String FALSE = "0";
	public static final String TRUE = "1";

	public void setValue(boolean value) {
		setValue(value ? TRUE : FALSE);
	}

	public boolean isActive() {
		return isActiveValue(TRUE);
	}

}
