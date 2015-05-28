package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.user.User;

/**
 * Represents pair of user and gate for saving it to server
 */
public class SaveUserPair {
	public final User user;
	public final String gateId;

	public SaveUserPair(final User user, final String gateId) {
		this.user = user;
		this.gateId = gateId;
	}
}