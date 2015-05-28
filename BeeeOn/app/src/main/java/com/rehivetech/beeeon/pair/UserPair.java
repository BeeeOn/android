package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.user.User;

/**
 * Represents pair of module and location for saving it to server
 */
public class UserPair {
	public final User user;
	public final String gateId;

	public UserPair(final User usr, final String adapter) {
		this.user = usr;
		this.gateId = adapter;
	}
}