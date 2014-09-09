package cz.vutbr.fit.iha.settings;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

/**
 * Enum for temperature unit
 * 
 * @author Martin Doudera
 * 
 */
public enum Timezone {
	ACTUAL("0", R.string.actual_timezone),
	ADAPTER("1", R.string.adapter_timezone);
	
	private final String mID;
	private final int mResName;

	private Timezone(String id, int resName) {
		this.mID = id;
		this.mResName = resName;
	}

	/**
	 * @return Default temperature unit
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
	 * @return List of IDs which will be saved in SharedPreferences.
	 */
	public static CharSequence[] getEntryValues() {
		List<String> retList = new ArrayList<String>();
		for (Timezone actTemp : Timezone.values()) {
			retList.add(actTemp.mID);
		}
		return retList.toArray(new CharSequence[retList.size()]);
	}

	/**
	 * Get Temperature by ID which will be saved in SharedPreferences.
	 * 
	 * @return If the ID exists, it returns Timezone object. Otherwise it
	 *         returns default timezone option.
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
			return getTimezoneById(prefs.getString(
					Constants.PERSISTANCE_PREF_TIMEZONE, getDefault().getId()));
	}
	
	///// CONVERTIONS
	/**
	 * Return offset from UTC in milliseconds
	 * @return
	 */
	private static int getLocalUtcOffset() {
		TimeZone tz = TimeZone.getDefault();
		Date now = new Date();
		return tz.getOffset(now.getTime());
	}
	
	private Time applyUtcOffset(Time time) {
		boolean useLocalTime = this.equals(ACTUAL);
		
		int utcOffset = useLocalTime ? getLocalUtcOffset() : 0; 
		Time result = new Time();
		result.set(time.toMillis(true) + utcOffset);
		return result;
	}
	
	public String formatLastUpdate(Time lastUpdate) {
		// Apply utcOffset
		lastUpdate = applyUtcOffset(lastUpdate);
		
		// Last update time data
		Time yesterday = new Time();
		yesterday.setToNow();
		yesterday.set(yesterday.toMillis(true) - 23 * 60 * 60 * 1000); // -23 hours
		
		// If sync time is more that 24 ago, show only date. Show time otherwise.
		DateFormat dateFormat = yesterday.before(lastUpdate) ? DateFormat.getTimeInstance() : DateFormat.getDateInstance();
		
		Date lastUpdateDate = new Date(lastUpdate.toMillis(true));
		return dateFormat.format(lastUpdateDate);
	}
		
}
