package com.rehivetech.beeeon.pair;

import java.util.EnumSet;

import com.rehivetech.beeeon.household.device.Device.SaveDevice;
import com.rehivetech.beeeon.household.device.Facility;

/**
 * Represents pair of device and location for saving it to server
 */
public class SaveFacilityPair {
	public final Facility facility;
	public final EnumSet<SaveDevice> what;

	public SaveFacilityPair(final Facility fac, final EnumSet<SaveDevice> what) {
		this.facility = fac;
		this.what = what;
	}
}