package com.rehivetech.beeeon.household.device.values;

import android.graphics.Color;

import com.rehivetech.beeeon.R;

public final class BoilerStatusValue extends BaseEnumValue {

	public static final String UNDEFINED = "0";
	public static final String HEATING = "1";
	public static final String HOT_WATER = "2";
	public static final String FAILURE = "3";
	public static final String SHUTDOWN = "4";

	public BoilerStatusValue() {
		super();

		// FIXME: Fix drawables and colors
		mItems.add(this.new Item(0, UNDEFINED, R.drawable.ic_dev_termostat_gray, R.string.dev_boiler_status_undefined, Color.BLACK));
		mItems.add(this.new Item(1, HEATING, R.drawable.ic_dev_termostat_gray, R.string.dev_boiler_status_heating, Color.BLACK));
		mItems.add(this.new Item(2, HOT_WATER, R.drawable.ic_dev_termostat_gray, R.string.dev_boiler_status_hot_water, Color.BLACK));
		mItems.add(this.new Item(3, FAILURE, R.drawable.ic_dev_termostat_gray, R.string.dev_boiler_status_failure, Color.BLACK));
		mItems.add(this.new Item(4, SHUTDOWN, R.drawable.ic_dev_termostat_gray, R.string.dev_boiler_status_shutdown, Color.BLACK));
	}

}
