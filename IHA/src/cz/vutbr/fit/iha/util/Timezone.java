package cz.vutbr.fit.iha.util;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
	ACTUAL("0", R.string.actual_timezone), //
	ADAPTER("1", R.string.adapter_timezone);

	private final String mID;
	private final int mResName;

	private Timezone(String id, int resName) {
		this.mID = id;
		this.mResName = resName;
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
	public String getId() {
		return mID;
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
	public static CharSequence[] getEntries(Context context) {
		List<String> retList = new ArrayList<String>();
		for (Timezone actTemp : Timezone.values()) {
			retList.add(actTemp.getName(context));
		}
		return retList.toArray(new CharSequence[retList.size()]);
	}

	/**
	 * @return List of IDs which will be saved in SharedPreferences
	 */
	public static CharSequence[] getEntryValues() {
		List<String> retList = new ArrayList<String>();
		for (Timezone actTemp : Timezone.values()) {
			retList.add(actTemp.mID);
		}
		return retList.toArray(new CharSequence[retList.size()]);
	}

	/**
	 * Get Timezone by ID which will be saved in SharedPreferences
	 * 
	 * @return If the ID exists, it returns Timezone object. Otherwise it returns default timezone option.
	 */
	private static Timezone getTimezoneById(String id) {
		for (Timezone actTemp : Timezone.values()) {
			if (actTemp.mID.equals(id)) {
				return actTemp;
			}
		}
		return getDefault();
	}

	public static Timezone getSharedPreferenceOption(SharedPreferences prefs) {
		return getTimezoneById(prefs.getString(Constants.PERSISTANCE_PREF_TIMEZONE, getDefault().getId()));
	}
	
	public DateTimeZone getDateTimeZone(Adapter adapter) {
		boolean useLocalTime = this.equals(ACTUAL) || adapter == null;
		return useLocalTime ? DateTimeZone.getDefault() : DateTimeZone.forOffsetMillis(adapter.getUtcOffsetMillis());
	}

	/**
	 * Return string with formatted time (if it is 23 hours ago, it show only date)
	 * 
	 * @param lastUpdate
	 * @param adapter If null, then it will use local timezone
	 * @return
	 */
	public String formatLastUpdate(DateTime lastUpdate, Adapter adapter) {
		boolean useLocalTime = this.equals(ACTUAL) || adapter == null;
		boolean isTooOld = lastUpdate.plusHours(23).isBeforeNow();
				
		DateTimeZone zone = useLocalTime ? DateTimeZone.getDefault() : DateTimeZone.forOffsetMillis(adapter.getUtcOffsetMillis()); 
		DateTimeFormatter fmt = isTooOld ? DateTimeFormat.shortDate() : DateTimeFormat.mediumTime();
		
		return fmt.withZone(zone).print(lastUpdate);
	}

}
