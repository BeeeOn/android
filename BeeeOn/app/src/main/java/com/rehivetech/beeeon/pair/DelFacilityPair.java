package com.rehivetech.beeeon.pair;

/**
 * Represents pair of module and location for saving it to server
 */
public class DelFacilityPair {
	public final String facilityId;
	public final String gateId;

	public DelFacilityPair(final String fac, final String adapter) {
		this.facilityId = fac;
		this.gateId = adapter;
	}
}