package cz.vutbr.fit.iha.pair;

import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;

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