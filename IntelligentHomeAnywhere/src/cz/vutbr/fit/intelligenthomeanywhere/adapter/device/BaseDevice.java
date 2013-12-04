/**
 * @brief Package for Devices that implements sensors
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import android.content.Context;

/**
 * @brief Abstract class for all devices
 * @author Robyer
 *
 */
public abstract class BaseDevice {
	protected boolean mInitialized;
	protected String mLocation;
	protected String mName;
	protected int mRefreshTime;	
	protected int mBattery;
	protected String mLog;
	protected boolean mLogging;
	protected String mInvolveTime;
	//protected String mValue;
	
	protected NetworkState mNetwork = new NetworkState();
			
	/**
	 * Class constructor
	 */
	public BaseDevice() {}
	
	/**
	 * Public class that implements structure
	 */
	public final class NetworkState {
		public int quality;
		public String address; 
	}
	
	/**
	 * Get numeric identifier representing type of this device
	 * @return
	 */
	public abstract int getType();
	
	/**
	 * Get resource for human readable string representing type of this device
	 * @return
	 */
	public abstract int getTypeStringResource();
	
	/**
	 * Get resource for unit string of this device
	 * @return
	 */
	public abstract int getUnitStringResource();
	
	/**
	 * Get resource for icon representing type of this device
	 * @return
	 */
	public abstract int getTypeIconResource();
	
	
	/**
	 * Get value as a string
	 * @return value
	 */
	public abstract String getStringValue();
	
	/**
	 * Get value as a integer if is it possible, zero otherwise
	 * @return value
	 */
	public abstract int getRawIntValue();
	
	/**
	 * Get value as a float if it is possible, zero otherwise
	 * @return value
	 */
	public abstract float getRawFloatValue();
	
	/**
	 * Setting value via string
	 * @param value
	 */
	public abstract void setValue(String value);
	
	/**
	 * Setting value via integer
	 * @param value
	 */
	public abstract void setValue(int value);
	
	/**
	 * Get value with unit as string
	 * @param context
	 * @return
	 */
	public String getStringValueUnit(Context context) {
		int unitRes = getUnitStringResource();
		String unit = unitRes > 0 ? context.getResources().getString(unitRes) : "";
		
		return String.format("%s %s", this.getStringValue(), unit);
	}
	
	/**
	 * Get refresh time
	 * @return refresh time
	 */
	public int getRefresh() {
		return mRefreshTime;
	}
		
	/**
	 * Setting refresh time
	 * @param time
	 */
	public void setRefresh(int time) {
		mRefreshTime = time;
	}
	
	/**
	 * Get name of device
	 * @return name
	 */
	public String getName() {
		return mName;
	}
		
	/**
	 * Setting name of device
	 * @param name
	 */
	public void setName(String name) {		
		mName = name;
	}
	
	/**
	 * Get name of location
	 * @return location
	 */
	public String getLocation() {
		return mLocation;
	}
	
	/**
	 * Setting name of location
	 * @param location
	 */
	public void setLocation(String location) {		
		mLocation = location;
	}
	
	/**
	 * Returning true if there is some logging file for device
	 * @return
	 */
	public boolean isLogging() {
		return mLogging;
	}

	/**
	 * Setting flag if there is logging file
	 * @param logging
	 */
	public void setLogging(boolean logging) {
		mLogging = logging;
	}

	/*public String getValue() {
		return mValue;
	}

	public void setValue(String value) {
		mValue = value;
	}*/
	
	/**
	 * Returning flag if device has been initialized yet
	 * @return
	 */
	public boolean isInitialized() {
		return mInitialized;
	}
	
	/**
	 * Setting flag for device initialization state
	 * @param initialized
	 */
	public void setInitialized(boolean initialized) {
		mInitialized = initialized;
	}
	
	/**
	 * Get state of battery
	 * @return battery
	 */
	public int getBattery() {
		return mBattery;
	}

	/**
	 * Setting state of battery
	 * @param battery
	 */
	public void setBattery(int battery) {
		mBattery = battery;
	}

	/**
	 * Get time of setting of device to system
	 * @return involve time
	 */
	public String getInvolveTime() {
		return mInvolveTime;
	}

	/**
	 * Setting involve time
	 * @param involved
	 */
	public void setInvolveTime(String involved) {
		mInvolveTime = involved;		
	}

	/**
	 * Get MAC address of device
	 * @return address
	 */
	public String getAddress() {
		return mNetwork.address;
	}

	/**
	 * Setting MAC address
	 * @param address
	 */
	public void setAddress(String address) {
		mNetwork.address = address;
	}

	/**
	 * Get value of signal quality
	 * @return quality
	 */
	public int getQuality() {
		return mNetwork.quality;
	}

	/**
	 * Setting quality
	 * @param quality
	 */
	public void setQuality(int quality) {
		mNetwork.quality = quality;
	}
	
	/**
	 * Get name/path to logging file
	 * @return log name
	 */
	public String getLog() {
		return mLog;
	}
	
	/**
	 * Setting log name (possibly not used - handle via server, when logging flag is set)
	 * @param log
	 */
	public void setLog(String log) {
		mLog = log;
	}

	/**
	 * Send to server
	 * @return
	 */
	public boolean saveSettings() {
		// TODO: save settings to server
		return true;
	}

}
