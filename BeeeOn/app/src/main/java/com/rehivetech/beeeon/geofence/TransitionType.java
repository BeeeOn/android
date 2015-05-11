package com.rehivetech.beeeon.geofence;

import com.google.android.gms.location.Geofence;
import com.rehivetech.beeeon.INameIdentifier;

/**
 * Created by Martin on 31. 3. 2015.
 */
public enum TransitionType implements INameIdentifier {
	IN("in", Geofence.GEOFENCE_TRANSITION_ENTER), OUT("out", Geofence.GEOFENCE_TRANSITION_EXIT);

	private String mNetworkString;
	private int mIntType;

	TransitionType(String networkString, int intType) {
		mNetworkString = networkString;
		mIntType = intType;
	}

	public String getName() {
		return mNetworkString;
	}

	public String getId() {
		return String.valueOf(mIntType);
	}

}
