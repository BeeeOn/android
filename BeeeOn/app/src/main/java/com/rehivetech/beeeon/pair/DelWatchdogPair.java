package com.rehivetech.beeeon.pair;

/**
 * Represents pair of module and location for saving it to server
 */
public class DelWatchdogPair {
	public final String watchdogId;
	public final String gateId;

	public DelWatchdogPair(final String wat, final String adapter) {
		this.watchdogId = wat;
		this.gateId = adapter;
	}
}