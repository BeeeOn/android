package com.rehivetech.beeeon.adapter.watchdog;

import com.rehivetech.beeeon.R;

/**
 * @author mlyko
 */
public class WatchDogGeofenceType extends WatchDogBaseType {
	public static final int[] operatorIcons = {
			R.drawable.ic_in,
			R.drawable.ic_out
	};

	public static final String[] operatorCodes = {
			"in",
			"out"
	};

	WatchDogGeofenceType(){
		super(WatchDogOperatorType.GEOFENCE);
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
