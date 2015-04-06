package com.rehivetech.beeeon.pair;

import org.joda.time.Interval;

import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceLog.DataInterval;
import com.rehivetech.beeeon.household.device.DeviceLog.DataType;

/**
 * Represents "pair" of data required for get device log
 */
public class LogDataPair {
	public final Device device;
	public final Interval interval;
	public final DataType type;
	public final DataInterval gap;

	public LogDataPair(final Device device, final Interval interval, final DataType type, final DataInterval gap) {
		this.device = device;
		this.interval = interval;
		this.type = type;
		this.gap = gap;
	}
}