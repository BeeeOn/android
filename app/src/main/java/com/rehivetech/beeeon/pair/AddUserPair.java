package com.rehivetech.beeeon.pair;

import java.util.EnumSet;

import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Device.SaveDevice;
import com.rehivetech.beeeon.household.User;

/**
 * Represents pair of device and location for saving it to server
 */
public class AddUserPair {
	public final Adapter adapter;
	public final User user;

	public AddUserPair(final Adapter adapter, final User user) {
		this.adapter = adapter;
		this.user = user;
	}
}