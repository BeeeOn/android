package cz.vutbr.fit.iha.pair;

import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;

/**
 * Represents "pair" of data required for get device log
 */
public class LogDataPair {
	public final BaseDevice device;
	public final String from;
	public final String to;
	public final DataType type;
	public final DataInterval interval;

	public LogDataPair(final BaseDevice device, final String from, final String to, final DataType type, final DataInterval interval) {
		this.device = device;
		this.from = from;
		this.to = to;
		this.type = type;
		this.interval = interval;
	}
}