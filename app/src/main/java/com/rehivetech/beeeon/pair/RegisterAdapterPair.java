package com.rehivetech.beeeon.pair;

/**
 * Represents pair of adapter id and name for saving it to server
 */
public class RegisterAdapterPair {
	public final String adapterId;
	public final String adapterName;

	public RegisterAdapterPair(String adapterId, String adapterName) {
		this.adapterId = adapterId;
		this.adapterName = adapterName;
	}
}