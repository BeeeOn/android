package com.rehivetech.beeeon.household.device.values;

import android.graphics.Color;

import com.rehivetech.beeeon.R;

public final class OpenClosedValue extends BooleanValue {

	public static final String CLOSED = BooleanValue.FALSE;
	public static final String OPEN = BooleanValue.TRUE;

	public OpenClosedValue() {
		super();

		mItems.add(this.new Item(0, CLOSED, R.drawable.ic_module_win_closed_gray, R.string.dev_enum_value_closed, Color.RED));
		mItems.add(this.new Item(1, OPEN, R.drawable.ic_module_win_open_gray, R.string.dev_enum_value_open, Color.GREEN));
	}

}
