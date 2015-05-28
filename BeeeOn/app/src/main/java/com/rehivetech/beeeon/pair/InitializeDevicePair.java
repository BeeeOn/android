package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;

/**
 * Represents pair of module and location for saving it to server
 */
public class InitializeDevicePair {
	public final Device mDevice;
	public final Location location;

	public InitializeDevicePair(final Device device, final Location location) {
		this.mDevice = device;
		this.location = location;
	}
}