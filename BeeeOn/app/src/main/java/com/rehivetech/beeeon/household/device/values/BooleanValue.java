package com.rehivetech.beeeon.household.device.values;

import android.support.annotation.NonNull;

import java.util.Arrays;

public abstract class BooleanValue extends EnumValue {

	public static final String FALSE = "0";
	public static final String TRUE = "1";

	public BooleanValue(@NonNull Item falseItem, @NonNull Item trueItem) {
		super(Arrays.asList(falseItem, trueItem));
	}

	public void setValue(boolean value) {
		setValue(value ? TRUE : FALSE);
	}

	public boolean isActive() {
		return isActiveValue(TRUE);
	}

}
