package com.rehivetech.beeeon.model;

import android.util.Log;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.network.INetwork;

import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ModuleLogsModel extends BaseModel {

	private static final String TAG = ModuleLogsModel.class.getSimpleName();

	private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();

	private final Map<String, ModuleLog> mModulesLogs = new HashMap<>();

	public ModuleLogsModel(INetwork network) {
		super(network);
	}

	private void saveModuleLog(String moduleId, ModuleLog log) {
		ModuleLog data = mModulesLogs.get(moduleId);
		if (data == null) {
			// Just save new item
		}
			mModulesLogs.put(moduleId, log);
//		else {
			// We need to append these values to existing log
//			for (Entry<Long, Float> entry : log.getValues().entrySet()) {
//				data.addValue(entry.getKey(), entry.getValue());
//			}
//		}
	}

	private List<Interval> getMissingIntervals(ModuleLog.DataPair pair) {
		List<Interval> downloadIntervals = new ArrayList<>();
		Interval interval = pair.interval;
		String moduleId = pair.module.getModuleId().absoluteId;

		Log.d(TAG, String.format("We want interval: %s -> %s", fmt.print(interval.getStart()), fmt.print(interval.getEnd())));

		downloadIntervals.add(interval);
		return downloadIntervals;

//		if (!mModulesLogs.containsKey(moduleId)) {
//			// No log for this module, download whole interval
//			Log.d(TAG, String.format("No cached log for module %s", moduleId));
//			downloadIntervals.add(interval);
//		} else {
//			// We have this ModuleLog with (not necessarily all) values
//			ModuleLog data = mModulesLogs.get(moduleId);
//
//			// Values are returned as sorted
//			SortedMap<Long, Float> rows = data.getValues();
//			if (rows.isEmpty()) {
//				// No values in this log, download whole interval
//				Log.d(TAG, String.format("We have log, but with no values for module %s", moduleId));
//				downloadIntervals.add(interval);
//			} else {
//				// Use pair gap to make sure we won't make useless request when there won't be no new value anyway
//				int gap = pair.gap.getSeconds() * 1000;
//
//				// Determine missing interval
//				long first = rows.firstKey();
//				long last = rows.lastKey();
//
//				Log.d(TAG, String.format("We have cached: %s -> %s for module %s", fmt.print(first), fmt.print(last), moduleId));
//
//				Log.d(TAG, String.format("We have log and there are some values for module %s", moduleId));
//				Log.v(TAG, String.format("Gap: %d ms", gap));
//
//				if (interval.isBefore(first) || interval.isAfter(last)) {
//					// Outside of values in this log, download whole interval
//					Log.d(TAG, String.format("Wanted interval is before or after cached interval for module %s", moduleId));
//					downloadIntervals.add(interval);
//					// TODO: remember this new hole?
//				} else {
//					if (interval.contains(first)) {
//						Log.d(TAG, String.format("Wanted interval contains FIRST of cached interval for module %s", moduleId));
//						// <start of interval, start of saved data>
//						Interval cutInterval = new Interval(interval.getStartMillis(), first);
//						Log.v(TAG, String.format("Cut (FIRST) interval: %s -> %s for module %s", fmt.print(cutInterval.getStart()), fmt.print(cutInterval.getEnd()), moduleId));
//						Log.v(TAG, String.format("Is (FIRST) interval duration: %d > gap: %d ?", cutInterval.toDurationMillis(), gap));
//						if (cutInterval.toDurationMillis() > gap)
//							downloadIntervals.add(cutInterval);
//					}
//
//					if (interval.contains(last)) {
//						Log.d(TAG, String.format("Wanted interval contains LAST of cached interval for module %s", moduleId));
//						// <end of saved data, end of interval>
//						Interval cutInterval = new Interval(last, interval.getEndMillis());
//						Log.v(TAG, String.format("Cut (LAST) interval: %s -> %s for module %s", fmt.print(cutInterval.getStart()), fmt.print(cutInterval.getEnd()), moduleId));
//						Log.v(TAG, String.format("Is (LAST) interval duration: %d > gap: %d ?", cutInterval.toDurationMillis(), gap));
//						if (cutInterval.toDurationMillis() > gap)
//							downloadIntervals.add(cutInterval);
//					}
//				}
//			}
//		}
//
//		return downloadIntervals;
	}

	/**
	 * Return log for module.
	 *
	 * @param pair
	 * @return
	 */
	public ModuleLog getModuleLog(ModuleLog.DataPair pair) {
			ModuleLog log = new ModuleLog(pair.type, pair.gap);

		String moduleId = pair.module.getModuleId().absoluteId;
		if (mModulesLogs.containsKey(moduleId)) {
			// We have this ModuleLog, lets load wanted values from it
			ModuleLog data = mModulesLogs.get(moduleId);
			for (Entry<Long, Float> entry : data.getValues(pair.interval).entrySet()) {
				log.addValue(entry.getKey(), entry.getValue());
			}
		}

		return log;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param pair
	 * @return
	 */
	public synchronized boolean reloadModuleLog(ModuleLog.DataPair pair) throws AppException {
		List<Interval> downloadIntervals = getMissingIntervals(pair);

		String moduleId = pair.module.getModuleId().absoluteId;
		Log.i(TAG, String.format("%d missing intervals to download for module: %s", downloadIntervals.size(), moduleId));
		for (Interval interval : downloadIntervals) {
			Log.d(TAG, String.format("Missing interval: %s -> %s for module: %s", fmt.print(interval.getStart()), fmt.print(interval.getEnd()), moduleId));
		}

		try {
			for (Interval downloadInterval : downloadIntervals) {
				// Download selected partial log
				ModuleLog.DataPair downPair = new ModuleLog.DataPair(pair.module, downloadInterval, pair.type, pair.gap);
				ModuleLog log = mNetwork.devices_getLog(downPair.module.getDevice().getGateId(), downPair.module, downPair);

				// Save it
				saveModuleLog(moduleId, log);
			}
		} catch (AppException e) {
			throw AppException.wrap(e);
		}

		return true;
	}
}
