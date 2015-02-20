package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;

/**
 * Represents pair of device and location for saving it to server
 */
public class InitializeFacilityPair {
	public final Facility facility;
	public final Location location;

	public InitializeFacilityPair(final Facility facility, final Location location) {
		this.facility = facility;
		this.location = location;
	}
}