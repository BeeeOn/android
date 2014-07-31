package cz.vutbr.fit.iha.adapter.device;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;

/**
 * Represents history of values for device. 
 */
public class DeviceLog {

	private List<DataRow> mValues = new ArrayList<DataRow>(); // FIXME: use rather Map
	private DataType mType;
	private DataInterval mInterval;
	
	public enum DataType {
		MINIMUM("min"),
		AVERAGE("avg"),
		MEDIAN("med"),
		MAXIMUM("max"),
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
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid DataType value");
		}
	}
	
	public enum DataInterval {
		RAW(0),
		MINUTE(60),
		HOUR(60*60),
		DAY(60*60*24),
		WEEK(60*60*24*7),
		MONTH(60*60*24*7*4); // for server this is anything bigger than value of week
		
		private final int mValue;
		
		private DataInterval(int value) {
			mValue = value;
		}
		
		public int getValue() {
			return mValue;
		}
		
		public static DataInterval fromValue(int value) {
			for (DataInterval item : values()) {
				if (value <= item.getValue())
					return item;
			}
			throw new IllegalArgumentException("Invalid DataInterval value");
		}
	}
	
	public class DataRow {
		public static final String DATEFORMAT = "yyyy-MM-dd";
		public static final String TIMEFORMAT = "HH:mm:ss";
		
		public final Date date;
		public final Date time;
		public final float value;

		/**
		 * Constructor
		 * @param row from ContentLog message
		 */
		@SuppressLint("SimpleDateFormat")
		public DataRow(String row){
			// TODO: check this
			String[] parts = row.split("\\s+");
			
			Date date = null;
			Date time = null;
			
			try {
				date = new SimpleDateFormat(DATEFORMAT).parse(parts[0]);
				time = new SimpleDateFormat(TIMEFORMAT).parse(parts[1]);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			this.date = date;
			this.time = time;
			this.value = Float.parseFloat(parts[2]);
		}

		/**
		 * Method emulate toString method for debugging
		 * @return
		 */
		@SuppressLint("SimpleDateFormat")
		public String debugString() {
			return String.format("%s %s %s\n",
				new SimpleDateFormat(DATEFORMAT).format(date),
				new SimpleDateFormat(TIMEFORMAT).format(time),
				value);
		}
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
	}
	
	/**
	 * Return type of values in this log.
	 * @return
	 */
	public DataType getType() {
		return mType;
	}
	
	/**
	 * Return interval of values in this log.
	 * @return
	 */
	public DataInterval getInterval() {
		return mInterval;
	}
	
	/**
	 * Return all values from log
	 * @return list of rows or empty list
	 */
	public List<DataRow> getValues() {
		return mValues;
	}
	
	/**
	 * Return values between start and end date from log
	 * @param start
	 * @param end
	 * @return list of rows or empty list
	 */
	public List<DataRow> getValues(Date start, Date end) {
		List<DataRow> values = new ArrayList<DataRow>();
		
		for (DataRow row : mValues) {
			// FIXME: check also time - it shouldn't be saved separately but as something like DateTime from the beginning (= in Network)
			if (row.date.after(start) && row.date.before(end))
				values.add(row);
		}
		
		return values;
	}
	
	/**
	 * Add single value.
	 * @param row
	 */
	public void addValue(DataRow row) {
		mValues.add(row);
	}
	
	/**
	 * Clear and set all values. 
	 * @param rows
	 */
	public void setValues(List<DataRow> rows) {
		clearValues();
		mValues.addAll(rows);
	}
	
	/**
	 * Clear all values.
	 */
	public void clearValues() {
		mValues.clear();
	}

}
