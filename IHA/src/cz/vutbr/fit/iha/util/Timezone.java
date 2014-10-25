package cz.vutbr.fit.iha.util;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;

/**
 * Enum for timezone unit
 * 
 * @author Martin Doudera
 * 
 */
public enum Timezone {
	ACTUAL(0, R.string.actual_timezone), //
	ADAPTER(1, R.string.adapter_timezone);

	private final int mId;
	private final int mResName;

	private Timezone(int id, int resName) {
		this.mId = id;
		this.mResName = resName;
	}
	
	public static String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_TIMEZONE;
	}

	/**
	 * @return Default timezone unit
	 */
	public static Timezone getDefault() {
		return ACTUAL;
	}

	/**
	 * @return SharedPreference ID
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @param context
	 *            It can be app context
	 * @return Name of timezone option
	 */
	public String getName(Context context) {
		return context.getString(mResName);
	}

	/**
	 * @return List of values which will be visible for user
	 */
	public static String[] getNamesArray(Context context) {
		List<String> retList = new ArrayList<String>();
		for (Timezone actTemp : Timezone.values()) {
			retList.add(actTemp.getName(context));
		}
		return retList.toArray(new String[retList.size()]);
	}

	/**
	 * @return List of IDs which will be saved in SharedPreferences
	 */
	public static String[] getIdsArray() {
		List<String> retList = new ArrayList<String>();
		for (Timezone actTemp : Timezone.values()) {
			retList.add(String.valueOf(actTemp.mId));
		}
		return retList.toArray(new String[retList.size()]);
	}

	/**
	 * Get Timezone by ID which will be saved in SharedPreferences
	 * 
	 * @return If the ID exists, it returns Timezone object. Otherwise it returns default timezone option.
	 */
	private static Timezone getTimezoneByIdOrDefault(int id) {
		for (Timezone actTemp : Timezone.values()) {
			if (actTemp.mId == id) {
				return actTemp;
			}
		}
		return getDefault();
	}

	public static Timezone fromPreferences(SharedPreferences prefs) {
		String id = prefs.getString(getPersistenceKey(), String.valueOf(getDefault().getId()));
		return getTimezoneByIdOrDefault(Integer.parseInt(id));
	}

	public DateTimeZone getDateTimeZone(Adapter adapter) {
		boolean useLocalTime = this.equals(ACTUAL) || adapter == null;
		return useLocalTime ? DateTimeZone.getDefault() : DateTimeZone.forOffsetMillis(adapter.getUtcOffsetMillis());
	}

}
