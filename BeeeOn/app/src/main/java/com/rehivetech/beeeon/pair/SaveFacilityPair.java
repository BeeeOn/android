package com.rehivetech.beeeon.pair;

import java.util.EnumSet;

import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.device.Facility;

/**
 * Represents pair of module and location for saving it to server
 */
public class SaveFacilityPair {
	public final Facility facility;
	public final EnumSet<SaveModule> what;

	public SaveFacilityPair(final Facility fac, final EnumSet<SaveModule> what) {
		this.facility = fac;
		this.what = what;
	}
}