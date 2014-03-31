/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

/**
 * @author ThinkDeep
 *
 */
public class ContentRow {
	
	private Date mDate;
	private Date mTime;
	private int mValue;
	private int mBattery;
	
	public static final String DATEFORMAT = "yyyy-MM-dd";
	public static final String TIMEFORMAT = "HH:mm:ss";

	/**
	 * Constructor 
	 */
	public ContentRow() {}
	
	/**
	 * Constructor
	 * @param row from ContentLog message
	 */
	@SuppressLint("SimpleDateFormat")
	public ContentRow(String row){
		//TODO: check this
		String[] parts = row.split("\\s+");
		
		try {
			mDate = new SimpleDateFormat(DATEFORMAT).parse(parts[0]);
			mTime = new SimpleDateFormat(TIMEFORMAT).parse(parts[1]);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		mValue = Integer.parseInt(parts[2]);
		mBattery = Integer.parseInt(parts[3]);
	}
	
	/**
	 * Getter
	 * @return
	 */
	public int getValue(){
		return mValue;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public int getBattery(){
		return mBattery;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public Date getDate(){
		return mDate;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public Date getTime(){
		return mTime;
	}
	
	/**
	 * Method emulate toString method for debugging
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public String debugString(){
		String result = "";
		
		result += new SimpleDateFormat(DATEFORMAT).format(mDate) + " ";
		result += new SimpleDateFormat(TIMEFORMAT).format(mTime) + " ";
		result += mValue + " " + mBattery + "\n";
		
		return result;
	}

}
