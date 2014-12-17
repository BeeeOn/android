package cz.vutbr.fit.iha.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.exception.IhaException;
import cz.vutbr.fit.iha.network.DemoNetwork;
import cz.vutbr.fit.iha.network.INetwork;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.pair.LogDataPair;
import cz.vutbr.fit.iha.util.Log;

public class DeviceLogsModel {

	private static final String TAG = DeviceLogsModel.class.getSimpleName();
	
	private DateTimeFormatter fmt = DateTimeFormat.fullDateTime().withZoneUTC();
	
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
		
		Log.d(TAG, String.format("We want interval: %s -> %s", fmt.print(interval.getStart()), fmt.print(interval.getEnd())));
		
		if (!mDevicesLogs.containsKey(pair.device.getId())) {
			// No log for this device, download whole interval
			Log.d(TAG, String.format("No cached log for device %s", pair.device.getId()));
			downloadIntervals.add(interval);
		} else {
			// We have this DeviceLog with (not necessarily all) values
			DeviceLog data = mDevicesLogs.get(pair.device.getId());
			
			// Values are returned as sorted
			List<DeviceLog.DataRow> rows = data.getValues();
			if (rows.isEmpty()) {
				// No values in this log, download whole interval
				Log.d(TAG, String.format("We have log, but with no values for device %s", pair.device.getId()));
				downloadIntervals.add(interval);
			} else {
				// Use pair gap to make sure we won't make useless request when there won't be no new value anyway
				int gap = pair.gap.getValue() * 1000;
				
				// Determine missing interval
				long first = rows.get(0).dateMillis;
				long last = rows.get(rows.size()-1).dateMillis;
				
				Log.d(TAG, String.format("We have cached: %s -> %s for device %s", fmt.print(first), fmt.print(last), pair.device.getId()));
				
				Log.d(TAG, String.format("We have log and there are some values for device %s", pair.device.getId()));
				Log.d(TAG, String.format("Gap: %d ms", gap));
				
				if (interval.isBefore(first) || interval.isAfter(last)) {
					// Outside of values in this log, download whole interval
					Log.d(TAG, String.format("Wanted interval is before or after cached interval for device %s", pair.device.getId()));
					downloadIntervals.add(interval);
					// TODO: remember this new hole?
				} else {
					if (interval.contains(first)) {
						Log.d(TAG, String.format("Wanted interval contains FIRST of cached interval for device %s", pair.device.getId()));
						// <start of interval, start of saved data> 
						Interval cutInterval = new Interval(interval.getStartMillis(), first);
						Log.d(TAG, String.format("Cut (FIRST) interval: %s -> %s for device %s", fmt.print(cutInterval.getStart()), fmt.print(cutInterval.getEnd()), pair.device.getId()));
						Log.d(TAG, String.format("Is (FIRST) interval duration: %d > gap: %d ?", cutInterval.toDurationMillis(), gap));
						if (cutInterval.toDurationMillis() > gap)
							downloadIntervals.add(cutInterval);
					}
					
					if (interval.contains(last)) {
						Log.d(TAG, String.format("Wanted interval contains LAST of cached interval for device %s", pair.device.getId()));
						// <end of saved data, end of interval>
						Interval cutInterval = new Interval(last, interval.getEndMillis());
						Log.d(TAG, String.format("Cut (LAST) interval: %s -> %s for device %s", fmt.print(cutInterval.getStart()), fmt.print(cutInterval.getEnd()), pair.device.getId()));
						Log.d(TAG, String.format("Is (LAST) interval duration: %d > gap: %d ?", cutInterval.toDurationMillis(), gap));
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
		
		Log.i(TAG, String.format("%d missing intervals", downloadIntervals.size()));
		for (Interval interval : downloadIntervals) {
			Log.i(TAG, String.format("Missing interval: %s -> %s", fmt.print(interval.getStart()), fmt.print(interval.getEnd())));
		}

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
