package cz.vutbr.fit.iha.pair;

import org.joda.time.Interval;

import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;

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