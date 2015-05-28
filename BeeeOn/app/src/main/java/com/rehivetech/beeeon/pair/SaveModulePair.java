package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;

import java.util.EnumSet;

/**
 * Represents pair of module and location for saving it to server
 */
public class SaveModulePair {
	public final Module mModule;
	public final EnumSet<SaveModule> what;

	public SaveModulePair(final Module module, final EnumSet<SaveModule> what) {
		this.mModule = module;
		this.what = what;
	}
}