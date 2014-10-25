package cz.vutbr.fit.iha.adapter.device;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

import android.util.Log;

/**
 * Represents history of values for device.
 */
public class DeviceLog {
	private static final String TAG = DeviceLog.class.getSimpleName();

	private static final String DATA_SEPARATOR = "\\s+";

	private List<DataRow> mValues = new ArrayList<DataRow>(); // FIXME: use rather Map
	private DataType mType;
	private DataInterval mInterval;

	private float mMinValue;
	private float mMaxValue;

	public enum DataType {
		MINIMUM("min"), //
		AVERAGE("avg"), //
		MEDIAN("med"), //
		MAXIMUM("max"), //
		BATTERY("bat"); // for future use

		private final String mValue;

		private DataType(String value) {
			mValue = value;
		}

		public String getValue() {
			return mValue;
		}

		public static DataType fromValue(String value) {
			for (DataType item : values()) {
				if (value.equalsIgnoreCase(item.getValue())) {
					return item;
				}
			}
			throw new IllegalArgumentException("Invalid DataType value");
		}
	}

	public enum DataInterval {
		RAW(0), //
		MINUTE(60), //
		HOUR(60 * 60), //
		DAY(60 * 60 * 24), //
		WEEK(60 * 60 * 24 * 7), //
		MONTH(60 * 60 * 24 * 7 * 4); // for server this is anything bigger than value of week

		private final int mValue;

		private DataInterval(int value) {
			mValue = value;
		}

		public int getValue() {
			return mValue;
		}

		public static DataInterval fromValue(int value) {
			for (DataInterval item : values()) {
				if (value <= item.getValue()) {
					return item;
				}
			}
			throw new IllegalArgumentException("Invalid DataInterval value");
		}
	}

	public class DataRow {
		public final long dateMillis;
		public final float value;

		/**
		 * Constructor creates new DataRow from string
		 * 
		 * @param row
		 *            from ContentLog message
		 * @throws IllegalArgumentException
		 */
		public DataRow(String row) throws IllegalArgumentException {
			String[] parts = row.split(DATA_SEPARATOR);

			if (parts.length != 2) {
				Log.e(TAG, String.format("Wrong number of parts (%d) of data: %s", parts.length, row));
				throw new IllegalArgumentException();
			}

			this.dateMillis = Long.parseLong(parts[0])*1000;
			this.value = Float.parseFloat(parts[1]);
		}

		public DataRow(long dateMillis, float value){
			this.dateMillis = dateMillis;
			this.value = value;
		}
		
	}

	/**
	 * Constructor
	 */
	public DeviceLog() {
		clearValues();
	}

	/**
	 * Constructor.
	 * 
	 * @param type
	 * @param interval
	 */
	public DeviceLog(DataType type, DataInterval interval) {
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
	 * Return all values from log
	 * 
	 * @return list of rows or empty list
	 */
	public List<DataRow> getValues() {
		return mValues;
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
	 * Return values between start and end date from log
	 * 
	 * @param start
	 * @param end
	 * @return list of rows or empty list
	 */
	public List<DataRow> getValues(Interval interval) {
		List<DataRow> values = new ArrayList<DataRow>();

		for (DataRow row : mValues) {
			if (interval.contains(row.dateMillis)) {
				values.add(row);
			}
		}

		return values;
	}

	/**
	 * Add single value.
	 * 
	 * @param row
	 */
	public void addValue(DataRow row) {
		mValues.add(row);

		// Remember min/max values
		if (!Float.isNaN(row.value)) {
			mMinValue = Math.min(mMinValue, row.value);
			mMaxValue = Math.max(mMaxValue, row.value);
		}
	}
	
	/**
	 * Add interval of same values.
	 * 
	 * @param row
	 * @param repeat number of rows
	 * @param interval gap in seconds
	 * @return
	 */
	public void addValueInterval(DataRow row, int repeat, int interval) {
		for (int i = 0; i <= repeat; i++) {
			addValue(new DataRow(row.dateMillis + i*(interval * 1000), row.value));
		}
	}

	/**
	 * Clear and set all values.
	 * 
	 * @param rows
	 */
	public void setValues(List<DataRow> rows) {
		clearValues();

		for (DataRow row : rows) {
			addValue(row);
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

}
