package com.rehivetech.beeeon.util;

import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;

import java.util.List;
import java.util.Random;

public class ValuesGenerator {

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

}
