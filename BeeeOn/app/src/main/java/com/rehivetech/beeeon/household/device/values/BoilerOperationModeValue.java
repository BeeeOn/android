package com.rehivetech.beeeon.household.device.values;

import android.graphics.Color;

import com.rehivetech.beeeon.R;

public final class BoilerOperationModeValue extends BaseEnumValue {

	public static final String AUTOMATIC = "0";
	public static final String MANUAL = "1";
	public static final String VACATION = "2";

	public BoilerOperationModeValue() {
		super();

		// FIXME: Fix drawables and colors
		mItems.add(this.new Item(0, AUTOMATIC, R.drawable.ic_val_state_gray, R.string.dev_boiler_operation_mode_value_automatic, Color.BLACK));
		mItems.add(this.new Item(1, MANUAL, R.drawable.ic_val_state_gray, R.string.dev_boiler_operation_mode_value_manual, Color.BLACK));
		mItems.add(this.new Item(2, VACATION, R.drawable.ic_val_state_gray, R.string.dev_boiler_operation_mode_value_vacation, Color.BLACK));
	}

}
