package com.rehivetech.beeeon.household.device.values;

import android.graphics.Color;

import com.rehivetech.beeeon.R;

public final class OpenClosedValue extends BooleanValue {

	public static final String CLOSED = BooleanValue.FALSE;
	public static final String OPEN = BooleanValue.TRUE;

	public OpenClosedValue() {
		super();

		mItems.add(this.new Item(0, CLOSED, R.drawable.dev_state_closed, R.string.dev_enum_value_closed, Color.RED));
		mItems.add(this.new Item(1, OPEN, R.drawable.dev_state_open, R.string.dev_enum_value_open, Color.GREEN));
	}

}
