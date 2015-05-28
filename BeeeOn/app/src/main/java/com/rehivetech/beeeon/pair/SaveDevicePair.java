package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module.SaveModule;

import java.util.EnumSet;

/**
 * Represents pair of device and location for saving it to server
 */
public class SaveDevicePair {
	public final Device mDevice;
	public final EnumSet<SaveModule> what;

	public SaveDevicePair(final Device deviceId, final EnumSet<SaveModule> what) {
		this.mDevice = deviceId;
		this.what = what;
	}
}