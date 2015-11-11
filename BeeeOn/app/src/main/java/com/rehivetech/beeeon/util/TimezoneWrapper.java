package com.rehivetech.beeeon.util;

import android.support.annotation.NonNull;
import android.util.Log;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Robert on 8. 7. 2015.
 */
public class TimezoneWrapper implements Comparable<TimezoneWrapper> {
	public final DateTimeZone timezone;
	public final int offsetInMillis;

	private static List<TimezoneWrapper> mTimezones = null;

	private TimezoneWrapper(DateTimeZone timezone) {
		this.timezone = timezone;
		this.offsetInMillis = timezone.getOffset(null);
	}

	public static synchronized List<TimezoneWrapper> getTimezones() {
		if (mTimezones == null) {
			mTimezones = new ArrayList<>();
			String[] timezonesIds = new String[] {
					"Etc/GMT+12", "Etc/GMT+11", "Etc/GMT+10", "Pacific/Marquesas", "America/Adak", "America/Anchorage", "America/Creston",
					"America/Belize", "America/Atikokan", "America/Caracas", "America/Port_of_Spain", "America/Araguaina", "America/St_Johns",
					"America/Godthab", "Atlantic/Cape_Verde", "Africa/Abidjan", "Africa/Algiers", "Africa/Maputo", "Africa/Nairobi",
					"Asia/Dubai", "Asia/Kabul", "Antarctica/Mawson", "Asia/Kolkata", "Asia/Kathmandu", "Antarctica/Vostok", "Asia/Rangoon",
					"Antarctica/Davis", "Antarctica/Casey", "Australia/Eucla", "Asia/Dili", "Australia/Adelaide", "Antarctica/DumontDUrville",
					"Australia/Lord_Howe", "Antarctica/Macquarie", "Pacific/Norfolk", "Pacific/Auckland", "Pacific/Chatham", "Etc/GMT-13",
					"Etc/GMT-14"
			};
			for (String id : timezonesIds) {
				DateTimeZone zone = DateTimeZone.forID(id);

				TimezoneWrapper newTimeZone = new TimezoneWrapper(zone);
				mTimezones.add(newTimeZone);
			}
			Collections.sort(mTimezones);

			List<String> ids = new ArrayList<>();
			for (TimezoneWrapper zone : mTimezones) {
					ids.add(zone.timezone.getID());
			}
			Log.i("Timezones", Arrays.toString(ids.toArray()));

		}
		return mTimezones;
	}

	public static TimezoneWrapper getZoneByOffset(int offsetInMillis) {
		for (TimezoneWrapper timezone : getTimezones()) {
			if (timezone.offsetInMillis == offsetInMillis)
				return timezone;
		}

		DateTimeZone zone = DateTimeZone.forOffsetMillis(offsetInMillis);
		return new TimezoneWrapper(zone);
	}

	@Override
	public String toString() {
		Period period = Duration.millis(offsetInMillis).toPeriod();

		int hours = Math.abs(period.getHours());
		int minutes = Math.abs(period.getMinutes());

		String time = String.format("GMT%s%s:%s",
				offsetInMillis >= 0 ? "+" : "-",
				(hours < 10 ? "0" : "") + hours,
				(minutes < 10 ? "0" : "") + minutes);

		String name = timezone.toTimeZone().getDisplayName();
		if (name.equalsIgnoreCase(time))
			return time;

		return String.format("%s %s", time, name);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof TimezoneWrapper) && this.offsetInMillis == ((TimezoneWrapper) o).offsetInMillis;
	}

	@Override
	public int compareTo(@NonNull TimezoneWrapper another) {
		int lhs = offsetInMillis;
		int rhs = another.offsetInMillis;

		return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
	}
}
