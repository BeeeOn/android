package com.rehivetech.beeeon.util;

import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;

import java.util.List;
import java.util.Random;

import timber.log.Timber;

public class ValuesGenerator {

	private static final int RAW_ENUM_VALUES_COUNT_IN_LOG = 100;

	public static String generateValue(Module module, Random rand) {
		// For enum values simply choose some random value
		if (module.getValue() instanceof EnumValue) {
			EnumValue value = (EnumValue) module.getValue();
			List<EnumValue.Item> items = value.getEnumItems();
			EnumValue.Item item = items.get(rand.nextInt(items.size()));

			return item.getValue();
		}

		BaseValue value = module.getValue();
		BaseValue.Constraints constraints = value.getConstraints();

		double min = constraints != null && constraints.getMin() != null ? constraints.getMin() : value.getSaneMinimum();
		double max = constraints != null && constraints.getMax() != null ? constraints.getMax() : value.getSaneMaximum();
		double step = constraints != null && constraints.getGranularity() != null ? constraints.getGranularity() : value.getSaneStep();

		if (Double.isNaN(min) || Double.isNaN(max) || Double.isNaN(step)) {
			min = 0;
			max = 100;
			step = 1;
		}

		double lastValue = module.getValue().getDoubleValue();

		if (!module.getValue().hasValue() || Double.isNaN(lastValue)) {
			// Generate random first value
			Double next = (((int) (((max - min) / step) / 2) * step)) + min;
			return String.valueOf(next);
		}

		// Generate next value
		RefreshInterval refresh = module.getDevice().getRefresh();
		int changes = (refresh != null ? refresh.getIntervalIndex() + 1 : 1);
		double newValue = lastValue + (changes * step) * (rand.nextBoolean() ? 1 : -1);

		newValue = Math.max(min, Math.min(max, newValue));
		return (((int) newValue) == newValue ? String.valueOf((int) lastValue) : String.valueOf(lastValue));
	}

	public static ModuleLog generateLog(Module module, ModuleLog.DataPair pair, Random rand) {
		ModuleLog log = new ModuleLog(pair.type, pair.gap);

		long start = pair.interval.getStartMillis();
		long end = pair.interval.getEndMillis();

		BaseValue value = module.getValue();
		BaseValue.Constraints constraints = value.getConstraints();

		double min = constraints != null && constraints.getMin() != null ? constraints.getMin() : value.getSaneMinimum();
		double max = constraints != null && constraints.getMax() != null ? constraints.getMax() : value.getSaneMaximum();
		double step = constraints != null && constraints.getGranularity() != null ? constraints.getGranularity() : value.getSaneStep();

		if (Double.isNaN(min) || Double.isNaN(max) || Double.isNaN(step)) {
			min = 0;
			max = 100;
			step = 1;
		}

		double lastValue = pair.module.getValue().getDoubleValue();

		if (!module.getValue().hasValue() || Double.isNaN(lastValue)) {
			// Generate random first value
			lastValue = (((int) (((max - min) / step) / 2) * step)) + min;
		}

		boolean isEnum = (module.getValue() instanceof EnumValue);

		RefreshInterval refresh = module.getDevice().getRefresh();

		// use refresh interval for raw data, or 1 second when has no refresh
		long refreshMsecs = (refresh != null ? refresh.getInterval() * 1000 : 1000 * 60);
		long everyMsecs = pair.gap == ModuleLog.DataInterval.RAW ? refreshMsecs : pair.gap.getSeconds() * 1000;

		// Generate vales at minimum frequency of refresh interval (as server would do, then chart helper will transform them into time axis)
		everyMsecs = Math.max(everyMsecs, refreshMsecs);

		int changes = (refresh != null ? refresh.getIntervalIndex() + 1 : 1);

		Timber.d("Filling %d values", (end - start) / everyMsecs);

		while (start < end) {
			// First make decision if we want any change
			if (rand.nextBoolean()) {
				if (isEnum) {
					List<EnumValue.Item> items = ((EnumValue) module.getValue()).getEnumItems();

					int pos = 0;
					for (EnumValue.Item item : items) {
						if (item.getId() == (int) lastValue) {
							break;
						}
						pos++;
					}
					// (size + pos + <-1,1>) % size  - first size is because it could end up to "-1"
					pos = (items.size() + pos + (rand.nextInt(3) - 1)) % items.size();
					lastValue = items.get(pos).getId();
				} else {
					lastValue = lastValue + (changes * step) * (rand.nextBoolean() ? 1 : -1);
				}

				lastValue = Math.max(min, Math.min(max, lastValue));
			}

			log.addValue(start, (float) lastValue);
			start += everyMsecs;
		}

		return log;
	}

}
