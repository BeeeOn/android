package com.rehivetech.beeeon.pair;

import java.util.EnumSet;

import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveDevice;

/**
 * Represents pair of module and location for saving it to server
 */
public class SaveDevicePair {
	public final Module mModule;
	public final EnumSet<SaveDevice> what;

	public SaveDevicePair(final Module module, final EnumSet<SaveDevice> what) {
		this.mModule = module;
		this.what = what;
	}
}