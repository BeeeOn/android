package com.rehivetech.beeeon.pair;

/**
 * Represents pair of gate id and name for saving it to server
 */
public class RegisterGatePair {
	public final String adapterId;
	public final String adapterName;

	public RegisterGatePair(String adapterId, String adapterName) {
		this.adapterId = adapterId;
		this.adapterName = adapterName;
	}
}