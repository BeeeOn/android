package cz.vutbr.fit.iha.util;

import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.adapter.device.units.BaseUnit;
import cz.vutbr.fit.iha.adapter.device.values.BaseEnumValue;
import cz.vutbr.fit.iha.adapter.device.values.BaseValue;
import cz.vutbr.fit.iha.adapter.device.values.UnknownValue;

public class UnitsHelper {

	private final SharedPreferences mPrefs;
	private final Context mContext;

	public UnitsHelper(SharedPreferences prefs, Context context) {
		mPrefs = prefs;
		mContext = context;
	}

	public String getStringValue(BaseValue item, double value) {
		if (item instanceof BaseEnumValue) {
			int resId = ((BaseEnumValue) item).getItemByDoubleValue(value).getStringResource();
			return mContext.getString(resId);
		}

		BaseUnit.Item to = (BaseUnit.Item) item.getUnit().fromSettings(mPrefs);
		double d = item.getUnit().convertValue(to, value);
		return Utils.formatDouble(d);
	}

	public String getStringValue(BaseValue item) {
		if (item instanceof BaseEnumValue) {
			int resId = ((BaseEnumValue) item).getStateStringResource();
			return mContext.getString(resId);
		} else if (item instanceof UnknownValue) {
			return ((UnknownValue) item).getRawValue();
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
