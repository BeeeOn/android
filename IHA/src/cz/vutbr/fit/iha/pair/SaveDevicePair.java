package cz.vutbr.fit.iha.pair;

import java.util.EnumSet;

import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;

/**
 * Represents pair of device and location for saving it to server
 */
public class SaveDevicePair {
	public final BaseDevice device;
	public final EnumSet<SaveDevice> what;

	public SaveDevicePair(final BaseDevice device, final EnumSet<SaveDevice> what) {
		this.device = device;
		this.what = what;
	}
}