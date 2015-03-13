package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device.SaveDevice;
import com.rehivetech.beeeon.adapter.device.Facility;

import java.util.EnumSet;

/**
 * Represents pair of device and location for saving it to server
 */
public class DelFacilityPair {
	public final String facilityID;
	public final String adapterID;

	public DelFacilityPair(final String fac, final String adapter) {
		this.facilityID = fac;
		this.adapterID = adapter;
	}
}