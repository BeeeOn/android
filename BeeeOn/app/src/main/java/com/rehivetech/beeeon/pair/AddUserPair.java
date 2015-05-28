package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;

/**
 * Represents pair of module and location for saving it to server
 */
public class AddUserPair {
	public final Gate mGate;
	public final User user;

	public AddUserPair(final Gate gate, final User user) {
		this.mGate = gate;
		this.user = user;
	}
}