package com.rehivetech.beeeon.pair;

import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog.DataInterval;
import com.rehivetech.beeeon.household.device.ModuleLog.DataType;

import org.joda.time.Interval;

/**
 * Represents "pair" of data required for get module log
 */
public class LogDataPair {
	public final Module module;
	public final Interval interval;
	public final DataType type;
	public final DataInterval gap;

	public LogDataPair(final Module module, final Interval interval, final DataType type, final DataInterval gap) {
		this.module = module;
		this.interval = interval;
		this.type = type;
		this.gap = gap;
	}
}