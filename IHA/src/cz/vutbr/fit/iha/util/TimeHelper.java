package cz.vutbr.fit.iha.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.SharedPreferences;
import cz.vutbr.fit.iha.adapter.Adapter;

public class TimeHelper {

	private SharedPreferences mPrefs;
	//private Adapter mAdapter;
	
	public TimeHelper(SharedPreferences prefs/*, Adapter adapter*/) {
		mPrefs = prefs;
		//mAdapter = adapter;
	}
	
	/*private final DateTimeZone mTimezone;

	public TimeFormatter(SharedPreferences prefs, Adapter adapter) {
		mTimezone = Timezone.fromPreferences(prefs).getDateTimeZone(adapter);
	}*/

	public DateTimeFormatter getFormatter(String pattern, Adapter adapter) {
		DateTimeZone zone = Timezone.fromPreferences(mPrefs).getDateTimeZone(adapter);
		return DateTimeFormat.forPattern(pattern).withZone(zone); 
	}
	
	/**
	 * Return string with formatted time (if it is 23 hours ago, it show only date)
	 * 
	 * @param lastUpdate
	 * @param adapter
	 *            If null, then it will use local timezone
	 * @return
	 */
	public String formatLastUpdate(DateTime lastUpdate, Adapter adapter) {
		DateTimeZone zone = Timezone.fromPreferences(mPrefs).getDateTimeZone(adapter);		
		
		boolean isTooOld = lastUpdate.plusHours(23).isBeforeNow();
		DateTimeFormatter fmt = isTooOld ? DateTimeFormat.shortDate() : DateTimeFormat.mediumTime();
		
		return fmt.withZone(zone).print(lastUpdate);
	}

}
