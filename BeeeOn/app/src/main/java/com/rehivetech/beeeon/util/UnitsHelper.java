package com.rehivetech.beeeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.household.device.units.BaseUnit;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;
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
			int resId = ((EnumValue) item).getItemByDoubleValue(value).getStringResource();
			return mContext.getString(resId);
		}

		BaseUnit.Item to = (BaseUnit.Item) item.getUnit().fromSettings(mPrefs);
		double d = item.getUnit().convertValue(to, value);
		return Utils.formatDouble(d);
	}

	public String getStringValue(BaseValue item) {
		if (item instanceof EnumValue) {
			int resId = ((EnumValue) item).getStateStringResource();
			return mContext.getString(resId);
		} else if (item instanceof UnknownValue) {
			return item.getRawValue();
		}

		return getStringValue(item, item.getDoubleValue());
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

}
