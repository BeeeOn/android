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
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.util.Utils;

/**
 * Enum for temperature unit
 * 
 * @author Martin Doudera
 * 
 */
public enum Timezone {
	ACTUAL("0", R.string.actual_timezone), ADAPTER("1", R.string.adapter_timezone);

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
	 * Get Temperature by ID which will be saved in SharedPreferences
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

	// /// CONVERTIONS
	/**
	 * @return offset from UTC in milliseconds
	 */
	private int getLocalUtcOffset() {
		TimeZone tz = TimeZone.getDefault();
		Date now = new Date();
		return tz.getOffset(now.getTime());
	}
	
	/**
	 * @param adapter
	 * @return adapter offset in milliseconds
	 */
	private int getAdapterUtcOffset(Adapter adapter) {
		// Adapter have it in minutes, so we convert it to milliseconds
		return adapter.getUtcOffset() * 60 * 1000;
	}

	/**
	 * @param time
	 * @param offsetMillis UTC offset in milliseconds
	 * @return new Time object with applied UTC offset
	 */
	private Time applyUtcOffset(Time time, int offsetMillis) {
		Time result = new Time();
		result.set(time.toMillis(true) + offsetMillis);
		return result;
	}

	/**
	 * Return string with formatted time (if it is 23 hours ago, it show only date)
	 * 
	 * @param lastUpdate
	 * @param adapter If null, then it will use local timezone
	 * @return
	 */
	public String formatLastUpdate(Time lastUpdate, Adapter adapter) {
		boolean useLocalTime = this.equals(ACTUAL) || adapter == null;
		int utcOffsetMillis = useLocalTime ? getLocalUtcOffset() : getAdapterUtcOffset(adapter);
		
		// Apply utcOffset
		lastUpdate = applyUtcOffset(lastUpdate, utcOffsetMillis);

		// If sync time is more that 23 ago, show only date. Show time otherwise.
		DateFormat dateFormat = Utils.isExpired(lastUpdate, 23 * 60 * 60) ? DateFormat.getDateInstance() : DateFormat.getTimeInstance();
		
		Date lastUpdateDate = new Date(lastUpdate.toMillis(true));
		return dateFormat.format(lastUpdateDate);
	}

}
