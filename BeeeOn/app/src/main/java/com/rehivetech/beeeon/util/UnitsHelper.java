package com.rehivetech.beeeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.household.device.units.BaseUnit;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.device.values.UnknownValue;

public class UnitsHelper {

	private final SharedPreferences mPrefs;
	private final Context mContext;

	public UnitsHelper(SharedPreferences prefs, Context context) {
		mPrefs = prefs;
		mContext = context.getApplicationContext();
	}

	public String getStringValue(BaseValue item, double value) {
		if (item instanceof EnumValue) {
			return ((EnumValue) item).getItemByDoubleValue(value).getName(mContext);
		}

		BaseUnit.Item to = item.getUnit().fromSettings(mPrefs);
		double d = item.getUnit().convertValue(to, value);
		return Utils.formatDouble(d);
	}

	public String getStringValue(BaseValue item) {
		if (item instanceof EnumValue) {
			return ((EnumValue) item).getState(mContext);
		} else if (item instanceof UnknownValue) {
			return item.getRawValue();
		}

		return getStringValue(item, item.getDoubleValue());
	}

	/**
	 * Converts value specified by units conversion method
	 *
	 * @param item determines which units should be used
	 * @param value from which will be converted
	 * @return converted double value
	 * @throws Exception
	 */
	public double getDoubleValue(BaseValue item, double value) throws Exception {
		if (item instanceof EnumValue) {
			throw new Exception("Put EnumValue to getDoubleValue() which accepts only numeric values");
		}

		BaseUnit.Item to = (BaseUnit.Item) item.getUnit().fromSettings(mPrefs);
		return item.getUnit().convertValue(to, value);
	}

	public String getStringUnit(BaseValue item) {
		return ((BaseUnit.Item) item.getUnit().fromSettings(mPrefs)).getStringUnit(mContext);
	}

	public String getStringValueUnit(BaseValue item) {
		String value = getStringValue(item);
		String unit = getStringUnit(item);

		if (unit.isEmpty())
			return value;

		return String.format("%s %s", value, unit);
	}

	/**
	 * Tries to format value by formatter in UnitsHelper, if is null, formats by default format
	 *
	 * @param helper UnitsHelper (might be null)
	 * @param value  to show
	 * @return formatted string
	 */
	public static String format(UnitsHelper helper, BaseValue value) {
		if (helper != null) {
			return helper.getStringValueUnit(value);
		} else {
			return String.format("%.2f %s", value.getDoubleValue(), value.getUnit());
		}
	}
}
