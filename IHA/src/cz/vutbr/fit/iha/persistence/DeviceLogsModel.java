package cz.vutbr.fit.iha.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Interval;

import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.exception.IhaException;
import cz.vutbr.fit.iha.network.DemoNetwork;
import cz.vutbr.fit.iha.network.INetwork;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.pair.LogDataPair;

public class DeviceLogsModel {

	//private static final String TAG = SensorDetailFragment.class.getSimpleName();
	
	private final INetwork mNetwork;

	private final Map<String, DeviceLog> mDevicesLogs = new HashMap<String, DeviceLog>();

	public DeviceLogsModel(INetwork network) {
		mNetwork = network;
	}
	
	private void saveDeviceLog(String deviceId, DeviceLog log) {
		DeviceLog data = mDevicesLogs.get(deviceId);
		if (data == null) {
			// Just save new item
			mDevicesLogs.put(deviceId, log);
		} else {
			// We need to append these values to existing log
			for (DeviceLog.DataRow row : log.getValues()) {
				data.addValue(row);
			}
		}
	}

	private List<Interval> getMissingIntervals(LogDataPair pair) {
		List<Interval> downloadIntervals = new ArrayList<Interval>();
		Interval interval = pair.interval;
		
		if (!mDevicesLogs.containsKey(pair.device.getId())) {
			// No log for this device, download whole interval
			downloadIntervals.add(interval);
		} else {
			// We have this DeviceLog with (not necessarily all) values
			DeviceLog data = mDevicesLogs.get(pair.device.getId());
			
			// Values are returned as sorted
			List<DeviceLog.DataRow> rows = data.getValues();
			if (rows.isEmpty()) {
				// No values in this log, download whole interval
				downloadIntervals.add(interval);
			} else {
				// Use pair gap to make sure we won't get duplicated value at interval borders
				int gap = pair.gap.getValue() * 1000;
				
				// Determine missing interval
				long first = rows.get(0).dateMillis - gap;
				long last = rows.get(rows.size()-1).dateMillis + gap;
				
				if (interval.isBefore(first) || interval.isAfter(last)) {
					// Outside of values in this log, download whole interval
					downloadIntervals.add(interval);
					// TODO: remember this new hole?
				} else {
					if (interval.contains(first)) {
						// <start of interval, start of saved data> 
						Interval cutInterval = new Interval(interval.getStartMillis(), first);
						if (cutInterval.toDurationMillis() > gap)
							downloadIntervals.add(cutInterval);
					}
					
					if (interval.contains(last)) {
						// <end of saved data, end of interval>
						Interval cutInterval = new Interval(last, interval.getEndMillis());
						if (cutInterval.toDurationMillis() > gap)
							downloadIntervals.add(cutInterval);
					}
				}
			}
		}
		
		return downloadIntervals;
	}

	public DeviceLog getDeviceLog(LogDataPair pair) {
		DeviceLog log = new DeviceLog(DataType.AVERAGE, DataInterval.RAW);
		
		if (mDevicesLogs.containsKey(pair.device.getId())) {
			// We have this DeviceLog, lets load wanted values from it
			DeviceLog data = mDevicesLogs.get(pair.device.getId());
			for (DeviceLog.DataRow row : data.getValues(pair.interval)) {
				log.addValue(row);
			}
		}
		 
		return log;
	}

	public boolean reloadDeviceLog(LogDataPair pair) {
		List<Interval> downloadIntervals = getMissingIntervals(pair);

		boolean isDemoNetwork = mNetwork instanceof DemoNetwork;
		try {
			if (!isDemoNetwork) {
				((Network) mNetwork).multiSessionBegin();
			}
			
			for (Interval downloadInterval : downloadIntervals) {
				// Download selected partial log
				LogDataPair downPair = new LogDataPair(pair.device, downloadInterval, pair.type, pair.gap);	 
				DeviceLog log = mNetwork.getLog(downPair.device.getFacility().getAdapterId(), downPair.device, downPair);
				
				// Save it
				saveDeviceLog(downPair.device.getId(), log);
			}
		} catch (IhaException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (!isDemoNetwork) {
				((Network) mNetwork).multiSessionEnd();
			}
		}

		return true;
	}
}
