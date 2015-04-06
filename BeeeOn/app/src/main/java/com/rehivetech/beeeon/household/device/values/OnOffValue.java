package com.rehivetech.beeeon.household.device.values;

import android.graphics.Color;
import com.rehivetech.beeeon.R;

public class OnOffValue extends BaseEnumValue {

	public static final String OFF = "0";
	public static final String ON = "1";

	public OnOffValue() {
		super();

		mItems.add(this.new Item(0, OFF, R.drawable.dev_switch_off, R.string.dev_enum_value_off, Color.RED));
		mItems.add(this.new Item(1, ON, R.drawable.dev_switch_on, R.string.dev_enum_value_on, Color.GREEN));
	}

}
