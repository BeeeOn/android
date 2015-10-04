package com.rehivetech.beeeon.household.device;

import com.rehivetech.beeeon.IIdentifier;

import org.joda.time.Interval;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents history of values for module.
 */
public class ModuleLog {
	private SortedMap<Long, Float> mValues = new TreeMap<>();
	private DataType mType;
	private DataInterval mInterval;

	private float mMinValue;
	private float mMaxValue;

	public enum DataType implements IIdentifier {
		MINIMUM("min"), //
		AVERAGE("avg"), //
		MEDIAN("med"), //
		MAXIMUM("max"), //
		BATTERY("bat"); // for future use

		private final String mValue;

		DataType(String value) {
			mValue = value;
		}

		public String getId() {
			return mValue;
		}
	}

	public enum DataInterval {
		RAW(0), //
		MINUTE(60), //
		FIVE_MINUTES(5 * 60), //
		TEN_MINUTES(10 * 60), //
		HALF_HOUR(30 * 60), //
		HOUR(60 * 60), //
		DAY(60 * 60 * 24), //
		WEEK(60 * 60 * 24 * 7), //
		MONTH(60 * 60 * 24 * 7 * 4); // for server this is anything bigger than value of week

		private final int mValue;

		DataInterval(int value) {
			mValue = value;
		}

		public int getSeconds() {
			return mValue;
		}

		public static DataInterval fromSeconds(int value) {
			for (DataInterval item : values()) {
				if (value <= item.getSeconds()) {
					return item;
				}
			}
			throw new IllegalArgumentException("Invalid DataInterval value");
		}
	}

	/**
	 * Constructor
	 */
	public ModuleLog() {
		clearValues();
	}

	/**
	 * Constructor.
	 *
	 * @param type
	 * @param interval
	 */
	public ModuleLog(DataType type, DataInterval interval) {
		mType = type;
		mInterval = interval;
		clearValues(); // to reset min/max values
	}

	/**
	 * Return type of values in this log.
	 *
	 * @return
	 */
	public DataType getType() {
		return mType;
	}

	/**
	 * Return interval of values in this log.
	 *
	 * @return
	 */
	public DataInterval getInterval() {
		return mInterval;
	}

	/**
	 * Return minimum value in this log
	 *
	 * @return
	 */
	public float getMinimum() {
		return mMinValue;
	}

	/**
	 * Return maximum value in this log
	 *
	 * @return
	 */
	public float getMaximum() {
		return mMaxValue;
	}

	/**
	 * Return deviation between maximum and minimum value in this log
	 *
	 * @return
	 */
	public float getDeviation() {
		return mMaxValue - mMinValue;
	}

	/**
	 * Return all values from log
	 *
	 * @return sorted map of rows
	 */
	public SortedMap<Long, Float> getValues() {
		return mValues;
	}

	/**
	 * Return values between <start, end) date from log
	 *
	 * @param interval
	 * @return sorted map of rows (or empty map)
	 */

	public SortedMap<Long, Float> getValues(Interval interval) {
		return mValues.subMap(interval.getStartMillis(), interval.getEndMillis());
	}

	/**
	 * Add single value.
	 *
	 * @param dateMillis
	 * @param value
	 */
	public void addValue(Long dateMillis, Float value) {
		mValues.put(dateMillis, value);

		// Remember min/max values
		if (!Float.isNaN(value)) {
			mMinValue = Math.min(mMinValue, value);
			mMaxValue = Math.max(mMaxValue, value);
		}
	}

	/**
	 * Add interval of same values.
	 *
	 * @param dateMillis
	 * @param value
	 * @param repeat     number of rows
	 * @param gap        gap in seconds
	 */
	public void addValueInterval(Long dateMillis, Float value, int repeat, int gap) {
		for (int i = 0; i <= repeat; i++) {
			addValue(dateMillis + i * (gap * 1000), value);
		}
	}

	/**
	 * Clear all values and add all rows.
	 *
	 * @param rows
	 */
	public void setValues(SortedMap<Long, Float> rows) {
		clearValues();

		for (Entry<Long, Float> entry : rows.entrySet()) {
			addValue(entry.getKey(), entry.getValue());
		}
	}

	public void setDataType(DataType type) {
		mType = type;
	}

	public void setDataInterval(DataInterval interval) {
		mInterval = interval;
	}

	/**
	 * Clear all values.
	 */
	public void clearValues() {
		mValues.clear();
		mMinValue = Float.POSITIVE_INFINITY;
		mMaxValue = Float.NEGATIVE_INFINITY;
	}

	/**
	 * Represents "pair" of data required for get module log
	 */
	public static class DataPair {
		public final Module module;
		public final Interval interval;
		public final DataType type;
		public final DataInterval gap;

		public DataPair(final Module module, final Interval interval, final DataType type, final DataInterval gap) {
			this.module = module;
			this.interval = interval;
			this.type = type;
			this.gap = gap;
		}
	}

}
