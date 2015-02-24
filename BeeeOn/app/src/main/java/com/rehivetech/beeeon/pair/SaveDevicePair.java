package com.rehivetech.beeeon.pair;

import java.util.EnumSet;

import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Device.SaveDevice;

/**
 * Represents pair of device and location for saving it to server
 */
public class SaveDevicePair {
	public final Device device;
	public final EnumSet<SaveDevice> what;

	public SaveDevicePair(final Device device, final EnumSet<SaveDevice> what) {
		this.device = device;
		this.what = what;
	}
}