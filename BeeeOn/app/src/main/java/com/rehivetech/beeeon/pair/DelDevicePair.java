package com.rehivetech.beeeon.pair;

/**
 * Represents pair of device and gate
 */
public class DelDevicePair {
	public final String deviceId;
	public final String gateId;

	public DelDevicePair(final String deviceId, final String gateId) {
		this.deviceId = deviceId;
		this.gateId = gateId;
	}
}