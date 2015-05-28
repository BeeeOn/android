package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.location.Location;

import java.util.EnumSet;

/**
 * Represents pair of module and location for saving it to server
 */
public class SaveFacilityWithNewLocPair {
	public final Device mDevice;
	public final EnumSet<Module.SaveModule> what;
	public final Location location;

	public SaveFacilityWithNewLocPair(final Device fac, final Location newLoc, final EnumSet<Module.SaveModule> what) {
		this.mDevice = fac;
		this.what = what;
		this.location = newLoc;
	}
}