package com.rehivetech.beeeon.adapter.device.values;

import android.graphics.Color;
import com.rehivetech.beeeon.R;

public class OpenClosedValue extends BaseEnumValue {

	public static final String CLOSED = "0";
	public static final String OPEN = "1";

	public OpenClosedValue() {
		super();

		mItems.add(this.new Item(0, CLOSED, R.drawable.dev_state_closed, R.string.dev_enum_value_closed, Color.RED));
		mItems.add(this.new Item(1, OPEN, R.drawable.dev_state_open, R.string.dev_enum_value_open, Color.GREEN));
	}

}
