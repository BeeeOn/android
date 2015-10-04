package com.rehivetech.beeeon.household.device;

import android.content.Context;

import com.rehivetech.beeeon.R;

public enum RefreshInterval {
	SEC_1(1), //
	SEC_5(5), //
	SEC_10(10), //
	SEC_20(20), //
	SEC_30(30), //
	MIN_1(60), //
	MIN_5(60 * 5), //
	MIN_10(60 * 10), //
	MIN_15(60 * 15), //
	MIN_30(60 * 30), //
	HOUR_1(60 * 60), //
	HOUR_2(60 * 60 * 2), //
	HOUR_3(60 * 60 * 3), //
	HOUR_4(60 * 60 * 4), //
	HOUR_8(60 * 60 * 8), //
	HOUR_12(60 * 60 * 12), //
	HOUR_24(60 * 60 * 24); //

	private final int mSecs;

	RefreshInterval(int secs) {
		mSecs = secs;
	}

	/**
	 * Get interval as number of seconds
	 *
	 * @return
	 */
	public int getInterval() {
		return mSecs;
	}

	/**
	 * Get interval as a string
	 *
	 * @return interval
	 */
	public String getStringInterval(Context context) {
		if (mSecs < 60) {
			// seconds
			int value = mSecs;
			return context.getResources().getQuantityString(R.plurals.refresh_interval_interval_seconds, value, value);
		} else if (mSecs < 60 * 60) {
			// minutes
			int value = mSecs / 60;
			return context.getResources().getQuantityString(R.plurals.refresh_interval_interval_minutes, value, value);
		} else {
			// hours
			int value = mSecs / (60 * 60);
			return context.getResources().getQuantityString(R.plurals.refresh_interval_interval_hours, value, value);
		}
	}

	/**
	 * Return index of this interval
	 *
	 * @return
	 */
	public int getIntervalIndex() {
		int i = 0;
		for (RefreshInterval item : values()) {
			if (item == this)
				return i;
			i++;
		}
		throw new IllegalArgumentException("Invalid RefreshInterval value");
	}

	/**
	 * Return enum object from interval
	 *
	 * @param secs number of seconds
	 * @return
	 */
	public static RefreshInterval fromInterval(int secs) {
		for (RefreshInterval item : values()) {
			if (secs <= item.getInterval())
				return item;
		}
		throw new IllegalArgumentException("Invalid RefreshInterval interval");
	}

}
