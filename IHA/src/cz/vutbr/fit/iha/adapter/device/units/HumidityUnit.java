package cz.vutbr.fit.iha.adapter.device.units;

import android.content.Context;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.util.Utils;

public enum HumidityUnit implements IDeviceUnit {
	DEFAULT("-1", R.string.dev_humidity_unit, R.string.dev_humidity_unit);

	private final String mId;
	private final int mResUnitName;
	private final int mResUnitShortName;

	private HumidityUnit(String id, int resUnitName, int resUnitShortName) {
		this.mId = id;
		this.mResUnitName = resUnitName;
		this.mResUnitShortName = resUnitShortName;
	}

	@Override
	public String getUnit(Context context) {
		return context.getString(mResUnitShortName);
	}

	@Override
	public String getName(Context context) {
		return context.getString(mResUnitName);
	}

	@Override
	public String getNameWithUnit(Context context) {
		return String.format("%s (%s)", getName(context), getUnit(context));
	}
	
	@Override
	public String formatValue(float value) {
		return Utils.formatFloat(value);
	}
	
	public float convertValue(float value) {
		// FIXME: implement this
		return value;
	}

}
