package com.rehivetech.beeeon.pair;

/**
 * Represents pair of gate id and name for saving it to server
 */
public class RegisterGatePair {
	public final String gateId;
	public final String gateName;

	public RegisterGatePair(String gateId, String gateName) {
		this.gateId = gateId;
		this.gateName = gateName;
	}
}