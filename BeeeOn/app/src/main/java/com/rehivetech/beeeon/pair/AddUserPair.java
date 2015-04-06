package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.user.User;

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