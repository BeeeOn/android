package cz.vutbr.fit.iha.adapter.device.units;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

/**
 * Enum for temperature unit
 * 
 * @author Martin Doudera
 */
public enum TemperatureUnit implements IDeviceUnit {
	CELSIUS("0", R.string.dev_temperature_celsius_unit_full, R.string.dev_temperature_celsius_unit), //
	FAHRENHEIT("1", R.string.dev_temperature_fahrenheit_unit_full, R.string.dev_temperature_fahrenheit_unit), //
	KELVIN("2", R.string.dev_temperature_kelvin_unit_full, R.string.dev_temperature_kelvin_unit);

	private final String mId;
	private final int mResUnitName;
	private final int mResUnitShortName;

	private TemperatureUnit(String id, int resUnitName, int resUnitShortName) {
		this.mId = id;
		this.mResUnitName = resUnitName;
		this.mResUnitShortName = resUnitShortName;
	}

	/**
	 * @return Default temperature unit
	 */
	public static TemperatureUnit getDefault() {
		return CELSIUS;
	}

	/**
	 * @return SharedPreference ID
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Get short form for unit. For example for celsius you will get "°C".
	 * 
	 * @param context
	 *            It can be app context
	 * @return Short form for unit
	 */
	public String getUnit(Context context) {
		return context.getString(mResUnitShortName);
	}

	/**
	 * Get full name for unit. For example for celsius you will get "Celsius".
	 * 
	 * @param context
	 *            It can be app context
	 * @return String which
	 */
	public String getName(Context context) {
		return context.getString(mResUnitName);
	}

	/**
	 * Get full name with short form for unit. For example for celsius you will get "Celsius (°C)".
	 * 
	 * @param context
	 *            It can be app context
	 * @return String which
	 */
	public String getNameWithUnit(Context context) {
		return getName(context) + " (" + getUnit(context) + ")";
	}

	/**
	 * @return List of values (name and short form of unit (ex.: Celsius (°C))) which will be visible for user
	 */
	public static CharSequence[] getEntries(Context context) {
		List<String> retList = new ArrayList<String>();
		for (TemperatureUnit actTemp : TemperatureUnit.values()) {
			retList.add(actTemp.getNameWithUnit(context));
		}
		return retList.toArray(new CharSequence[retList.size()]);
	}

	/**
	 * @return List of IDs which will be saved in SharedPreferences.
	 */
	public static CharSequence[] getEntryValues() {
		List<String> retList = new ArrayList<String>();
		for (TemperatureUnit actTemp : TemperatureUnit.values()) {
			retList.add(actTemp.mId);
		}
		return retList.toArray(new CharSequence[retList.size()]);
	}

	/**
	 * Get Temperature by ID which will be saved in SharedPreferences.
	 * 
	 * @return If the ID exists, it returns Temperature object. Otherwise it returns default temperature unit.
	 */
	private static TemperatureUnit getTemperatureById(String id) {
		for (TemperatureUnit actTemp : TemperatureUnit.values()) {
			if (actTemp.mId.equalsIgnoreCase(id)) {
				return actTemp;
			}
		}
		return getDefault();
	}

	public static TemperatureUnit fromSettings(SharedPreferences prefs) {
		String id = prefs.getString(Constants.PERSISTENCE_PREF_TEMPERATURE, getDefault().getId());
		return getTemperatureById(id);
	}

	// /// CONVERTIONS

	public float convertValue(float value) {
		return TemperatureUnit.convertCelsius(this, value);
	}
	
	public static float convertCelsius(TemperatureUnit to, float value) {
		switch (to) {
		case FAHRENHEIT:
			return value * 9 / 5 + 32;
		case KELVIN:
			return value + 273.15f;
		default:
			return value;
		}
	}

}
