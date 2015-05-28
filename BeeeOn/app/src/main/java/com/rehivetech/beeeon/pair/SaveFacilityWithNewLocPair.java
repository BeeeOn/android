package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;

import java.util.EnumSet;

/**
 * Represents pair of module and location for saving it to server
 */
public class SaveFacilityWithNewLocPair {
	public final Facility facility;
	public final EnumSet<Module.SaveModule> what;
	public final Location location;

	public SaveFacilityWithNewLocPair(final Facility fac, final Location newLoc, final EnumSet<Module.SaveModule> what) {
		this.facility = fac;
		this.what = what;
		this.location = newLoc;
	}
}