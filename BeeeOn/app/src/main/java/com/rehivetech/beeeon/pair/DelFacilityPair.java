package com.rehivetech.beeeon.pair;

/**
 * Represents pair of device and location for saving it to server
 */
public class DelFacilityPair {
	public final String facilityID;
	public final String adapterID;

	public DelFacilityPair(final String fac, final String adapter) {
		this.facilityID = fac;
		this.adapterID = adapter;
	}
}