package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import timber.log.Timber;

public class ModuleLogsModel extends BaseModel {

	private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();

	private final MultipleDataHolder<ModuleLog> mModulesLogs = new MultipleDataHolder<>(); // moduleId => ModuleLog dataHolder

	public ModuleLogsModel(INetwork network) {
		super(network);
	}

	private void saveModuleLog(ModuleLog.DataPair pair, ModuleLog log) {
		String moduleId = pair.module.getModuleId().absoluteId;

		ModuleLog data = mModulesLogs.getObject(moduleId, log.getId());
		if (data == null) {
			// Just save new item
			mModulesLogs.addObject(moduleId, log);
		} else {
			// We need to append these values to existing log
			for (Entry<Long, Float> entry : log.getValues().entrySet()) {
				data.addValue(entry.getKey(), entry.getValue());
			}
		}
	}

	private List<Interval> getMissingIntervals(ModuleLog.DataPair pair) {
		List<Interval> downloadIntervals = new ArrayList<>();
		Interval interval = pair.interval;
		String moduleId = pair.module.getModuleId().absoluteId;
		ModuleLog log = new ModuleLog(pair.type, pair.gap);

		Timber.v("We want interval: %s -> %s (%s)", fmt.print(interval.getStart()), fmt.print(interval.getEnd()), log.getId());

		if (!mModulesLogs.hasObject(moduleId, log.getId())) {
			// No log for this module, download whole interval
			Timber.v("No cached log (%s) for module %s", log.getId(), moduleId);
			downloadIntervals.add(interval);
		} else {
			// We have this ModuleLog with (not necessarily all) values
			ModuleLog data = mModulesLogs.getObject(moduleId, log.getId());

			// Values are returned as sorted
			SortedMap<Long, Float> rows = data.getValues();
			if (rows.isEmpty()) {
				// No values in this log, download whole interval
				Timber.v("We have log (%s), but with no values for module %s", log.getId(), moduleId);
				downloadIntervals.add(interval);
			} else {
				// Use pair gap to make sure we won't make useless request when there won't be no new value anyway
				int gap = pair.gap.getSeconds() * 1000;

				// Determine missing interval
				long first = rows.firstKey();
				long last = rows.lastKey();

				Timber.v("We have cached: %s -> %s for module %s", fmt.print(first), fmt.print(last), moduleId);

				Timber.v("We have log (%s) and there are some values for module %s", log.getId(), moduleId);
				Timber.v("Gap: %d ms", gap);

				if (interval.isBefore(first) || interval.isAfter(last)) {
					// Outside of values in this log, download whole interval
					Timber.v("Wanted interval is before or after cached interval for module %s", moduleId);
					downloadIntervals.add(interval);
					// TODO: remember this new hole?
				} else {
					if (interval.contains(first)) {
						Timber.v("Wanted interval contains FIRST of cached interval for module %s", moduleId);
						// <start of interval, start of saved data> 
						Interval cutInterval = new Interval(interval.getStartMillis(), first);
						Timber.v("Cut (FIRST) interval: %s -> %s for module %s", fmt.print(cutInterval.getStart()), fmt.print(cutInterval.getEnd()), moduleId);
						Timber.v("Is (FIRST) interval duration: %d > gap: %d ?", cutInterval.toDurationMillis(), gap);
						if (cutInterval.toDurationMillis() > gap)
							downloadIntervals.add(cutInterval);
					}

					if (interval.contains(last)) {
						Timber.v("Wanted interval contains LAST of cached interval for module %s", moduleId);
						// <end of saved data, end of interval>
						Interval cutInterval = new Interval(last, interval.getEndMillis());
						Timber.v("Cut (LAST) interval: %s -> %s for module %s", fmt.print(cutInterval.getStart()), fmt.print(cutInterval.getEnd()), moduleId);
						Timber.v("Is (LAST) interval duration: %d > gap: %d ?", cutInterval.toDurationMillis(), gap);
						if (cutInterval.toDurationMillis() > gap)
							downloadIntervals.add(cutInterval);
					}
				}
			}
		}

		return downloadIntervals;
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
		if (mModulesLogs.hasObject(moduleId, log.getId())) {
			// We have this ModuleLog, lets load wanted values from it
			ModuleLog data = mModulesLogs.getObject(moduleId, log.getId());
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
		Timber.d("%d missing intervals to download for module: %s", downloadIntervals.size(), moduleId);
		for (Interval interval : downloadIntervals) {
			Timber.v("Missing interval: %s -> %s for module: %s", fmt.print(interval.getStart()), fmt.print(interval.getEnd()), moduleId);
		}

		try {
			for (Interval downloadInterval : downloadIntervals) {
				// Download selected partial log
				ModuleLog.DataPair downPair = new ModuleLog.DataPair(pair.module, downloadInterval, pair.type, pair.gap);
				ModuleLog log = mNetwork.devices_getLog(downPair.module.getDevice().getGateId(), downPair.module, downPair);

				// Save it
				saveModuleLog(downPair, log);
			}
		} catch (AppException e) {
			throw AppException.wrap(e);
		}

		return true;
	}
}
