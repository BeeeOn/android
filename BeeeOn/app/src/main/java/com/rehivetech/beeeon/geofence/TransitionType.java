package com.rehivetech.beeeon.geofence;

import com.google.android.gms.location.Geofence;

/**
 * Created by Martin on 31. 3. 2015.
 */
public enum TransitionType {
	IN("in",  Geofence.GEOFENCE_TRANSITION_ENTER), OUT("out", Geofence.GEOFENCE_TRANSITION_EXIT);

	private String mNetworkString;
	private int mIntType;

	TransitionType(String networkString, int intType) {
		mNetworkString = networkString;
		mIntType = intType;
	}

	public String getString() {
		return mNetworkString;
	}

	public int getInt() {
		return mIntType;
	}

	public static TransitionType fromInt(int intType) {
		for (TransitionType actTrans : TransitionType.values()) {
			if (actTrans.mIntType == intType) {
				return actTrans;
			}
		}
		throw new IllegalStateException();
	}
}
