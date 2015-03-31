package com.rehivetech.beeeon.adapter.watchdog;

import com.rehivetech.beeeon.R;

/**
 * @author mlyko
 */
public class WatchDogSensorType extends WatchDogBaseType {
	public static final int[] operatorIcons = {
			R.drawable.ic_action_next_item,
			R.drawable.ic_action_previous_item
	};

	public static final String[] operatorCodes = {
			"gt",
			"lt"
	};

	WatchDogSensorType(){
		super(WatchDogOperatorType.SENSOR);
	}

	@Override
	public int[] getAllIcons() {
		return operatorIcons;
	}

	@Override
	public String[] getAllCodes() {
		return operatorCodes;
	}

}
