package com.rehivetech.beeeon.pair;

/**
 * Represents pair of module and location for saving it to server
 */
public class DelWatchDogPair {
	public final String watchdogID;
	public final String adapterID;

	public DelWatchDogPair(final String wat, final String adapter) {
		this.watchdogID = wat;
		this.adapterID = adapter;
	}
}