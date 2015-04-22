package com.rehivetech.beeeon.household.device.values;

import android.graphics.Color;

import com.rehivetech.beeeon.R;

public final class BoilerOperationTypeValue extends BaseEnumValue {

	public static final String OFF = "0";
	public static final String ROOM = "1";
	public static final String EQUITERM = "2";
	public static final String STABLE = "3";
	public static final String TUV = "4";

	public BoilerOperationTypeValue() {
		super();

		// FIXME: Fix drawables and colors
		mItems.add(this.new Item(0, OFF, R.drawable.dev_unknown, R.string.dev_boiler_operation_type_value_off, Color.BLACK));
		mItems.add(this.new Item(1, ROOM, R.drawable.dev_unknown, R.string.dev_boiler_operation_type_value_room, Color.BLACK));
		mItems.add(this.new Item(2, EQUITERM, R.drawable.dev_unknown, R.string.dev_boiler_operation_type_value_equiterm, Color.BLACK));
		mItems.add(this.new Item(3, STABLE, R.drawable.dev_unknown, R.string.dev_boiler_operation_type_value_stable, Color.BLACK));
		mItems.add(this.new Item(4, TUV, R.drawable.dev_unknown, R.string.dev_boiler_operation_type_value_tuv, Color.BLACK));
	}

}
