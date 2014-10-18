package cz.vutbr.fit.iha.adapter.device.units;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.util.Utils;

public enum NoiseUnit implements IDeviceUnit {
	DECIBEL("0", R.string.dev_noise_decibel_unit_full, R.string.dev_noise_decibel_unit),
	BEL("1", R.string.dev_noise_bel_unit_full, R.string.dev_noise_bel_unit),
	NEPER("2", R.string.dev_noise_neper_unit_full, R.string.dev_noise_neper_unit);

	private final String mId;
	private final int mResUnitName;
	private final int mResUnitShortName;

	private NoiseUnit(String id, int resUnitName, int resUnitShortName) {
		this.mId = id;
		this.mResUnitName = resUnitName;
		this.mResUnitShortName = resUnitShortName;
	}
	
	/**
	 * @return Default unit
	 */
	public static NoiseUnit getDefault() {
		return DECIBEL;
	}

	/**
	 * @return SharedPreference ID
	 */
	public String getId() {
		return mId;
	}
	
	/**
	 * @return List of values (name and short form of unit) which will be visible for user
	 */
	public static CharSequence[] getEntries(Context context) {
		List<String> retList = new ArrayList<String>();
		for (NoiseUnit unit : NoiseUnit.values()) {
			retList.add(unit.getNameWithUnit(context));
		}
		return retList.toArray(new CharSequence[retList.size()]);
	}

	/**
	 * @return List of IDs which will be saved in SharedPreferences.
	 */
	public static CharSequence[] getEntryValues() {
		List<String> retList = new ArrayList<String>();
		for (NoiseUnit unit : NoiseUnit.values()) {
			retList.add(unit.mId);
		}
		return retList.toArray(new CharSequence[retList.size()]);
	}

	/**
	 * Get Temperature by ID which will be saved in SharedPreferences.
	 * 
	 * @return If the ID exists, it returns wanted unit, otherwise default unit.
	 */
	private static NoiseUnit getUnitById(String id) {
		for (NoiseUnit unit : NoiseUnit.values()) {
			if (unit.mId.equalsIgnoreCase(id)) {
				return unit;
			}
		}
		return getDefault();
	}

	public static NoiseUnit fromSettings(SharedPreferences prefs) {
		String id = prefs.getString(Constants.PERSISTENCE_PREF_NOISE, getDefault().getId());
		return getUnitById(id);
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
	
	public float convertValue(int value) {
		switch (this) {
		case BEL:
			return value * 0.1f;
		case NEPER:
			return (float)(value / (20 * Math.log10(Math.E)));
		default:
			return value;
		}
	}

}
