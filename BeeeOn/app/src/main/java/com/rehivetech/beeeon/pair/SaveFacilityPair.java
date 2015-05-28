package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module.SaveModule;

import java.util.EnumSet;

/**
 * Represents pair of module and location for saving it to server
 */
public class SaveFacilityPair {
	public final Device mDevice;
	public final EnumSet<SaveModule> what;

	public SaveFacilityPair(final Device fac, final EnumSet<SaveModule> what) {
		this.mDevice = fac;
		this.what = what;
	}
}