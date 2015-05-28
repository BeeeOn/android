package com.rehivetech.beeeon.pair;

/**
 * Represents pair of module and location for saving it to server
 */
public class DelWatchdogPair {
	public final String watchdogID;
	public final String adapterID;

	public DelWatchdogPair(final String wat, final String adapter) {
		this.watchdogID = wat;
		this.adapterID = adapter;
	}
}