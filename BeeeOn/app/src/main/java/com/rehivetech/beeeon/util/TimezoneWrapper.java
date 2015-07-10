package com.rehivetech.beeeon.util;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Robert on 8. 7. 2015.
 */
public class TimezoneWrapper implements Comparable<TimezoneWrapper> {
	public final DateTimeZone timezone;
	public final int offsetInMillis;

	private static List<TimezoneWrapper> mTimezones = null;

	private TimezoneWrapper(DateTimeZone timezone, int millis) {
		this.timezone = timezone;
		this.offsetInMillis = millis;
	}

	public static synchronized List<TimezoneWrapper> getTimezones() {
		if (mTimezones == null) {
			mTimezones = new ArrayList<>();
			for (String id : DateTimeZone.getAvailableIDs()) {
				int millis = DateTimeZone.forID(id).getOffset(null);
				DateTimeZone zone = DateTimeZone.forID(id);

				TimezoneWrapper newTimeZone = new TimezoneWrapper(zone, millis);
				if (!mTimezones.contains(newTimeZone))
					mTimezones.add(newTimeZone);
			}
			Collections.sort(mTimezones);
		}
		return mTimezones;
	}

	public static TimezoneWrapper getZoneByOffset(int offsetInMillis) {
		for (TimezoneWrapper timezone : getTimezones()) {
			if (timezone.offsetInMillis == offsetInMillis)
				return timezone;
		}

		DateTimeZone zone = DateTimeZone.forOffsetMillis(offsetInMillis);
		return new TimezoneWrapper(zone, offsetInMillis);
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
	public int compareTo(TimezoneWrapper another) {
		int lhs = offsetInMillis;
		int rhs = another.offsetInMillis;

		return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
	}
}
