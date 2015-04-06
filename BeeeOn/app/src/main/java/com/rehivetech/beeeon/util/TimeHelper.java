package com.rehivetech.beeeon.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.SharedPreferences;
import com.rehivetech.beeeon.household.adapter.Adapter;

public class TimeHelper {

	private SharedPreferences mPrefs;

	// private Adapter mAdapter;

	public TimeHelper(SharedPreferences prefs/* , Adapter adapter */) {
		mPrefs = prefs;
		// mAdapter = adapter;
	}

	/*
	 * private final DateTimeZone mTimezone;
	 * 
	 * public TimeFormatter(SharedPreferences prefs, Adapter adapter) { mTimezone = Timezone.fromPreferences(prefs).getDateTimeZone(adapter); }
	 */

	private boolean useLocalTimezone() {
		Timezone timezone = new Timezone();
		Timezone.Item item = (Timezone.Item) timezone.fromSettings(mPrefs);
		return item.getId() == Timezone.ACTUAL;
	}

	public DateTimeZone getDateTimeZone(Adapter adapter) {
		boolean useLocalTime = useLocalTimezone() || adapter == null;

		return useLocalTime ? DateTimeZone.getDefault() : DateTimeZone.forOffsetMillis(adapter.getUtcOffsetMillis());
	}

	public DateTimeFormatter getFormatter(String pattern, Adapter adapter) {
		DateTimeZone zone = getDateTimeZone(adapter);
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
		boolean isTooOld = lastUpdate.plusHours(23).isBeforeNow();
		DateTimeFormatter fmt = isTooOld ? DateTimeFormat.shortDate() : DateTimeFormat.mediumTime();

		DateTimeZone zone = getDateTimeZone(adapter);
		return fmt.withZone(zone).print(lastUpdate);
	}

	/**
	 * Return string with formatted date time
	 * 
	 * @param time
	 * @param adapter
	 *            If null, then it will use local timezone
	 * @return
	 */
	public String formatTime(DateTime time, Adapter adapter) {
		return DateTimeFormat.shortDateTime().withZone(getDateTimeZone(adapter)).print(time);
	}

}
