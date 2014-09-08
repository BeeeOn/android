/**
 * @brief Package for Devices that implements sensors
 */
package cz.vutbr.fit.iha.adapter.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * @brief Abstract class for all devices
 * @author Robyer
 */
public abstract class BaseDevice {
	protected boolean mInitialized;
	protected String mLocationId;
	protected String mName = "";
	protected RefreshInterval mRefreshInterval;	
	protected int mBattery;
	protected boolean mLogging;
	protected String mInvolveTime = "";
	protected boolean mVisibility;
	
	protected NetworkState mNetwork = new NetworkState();
	
	
	public final Time lastUpdate = new Time();
	
	/**
	 * Class constructor
	 */
	public BaseDevice() {
	}
	
	/**
	 * Public class that implements structure
	 */
	public final class NetworkState {
		public int quality;
		public String address = ""; 
	}
	
	/**
	 * Represents settings of device which could be saved to server
	 */
	public enum SaveDevice {
		SAVE_NAME,			// rename device
		SAVE_LOCATION,		// change location
		SAVE_VISIBILITY,	// change visibility				//NOTE: sending always
		SAVE_LOGGING,		// change logging on server
		SAVE_REFRESH,		// change refresh interval
		SAVE_VALUE,			// change value (of actor)
		SAVE_TYPE,			// change device's icon, etc.		//NOTE: what? type cannot be changed
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
		return String.format("%s %s", this.getStringValue(), getStringUnit(context));
	}
	
	/**
	 * Get unit as string
	 * @param context
	 * @return
	 */
	public String getStringUnit(Context context) {
		int unitRes = getUnitStringResource();
		return unitRes > 0 ? context.getString(unitRes) : "";
	}
	
	/**
	 * Get refresh interval
	 * @return refresh interval
	 */
	public RefreshInterval getRefresh() {
		return mRefreshInterval;
	}
		
	/**
	 * Setting refresh interval
	 * @param interval
	 */
	public void setRefresh(RefreshInterval interval) {
		mRefreshInterval = interval;
	}
	
	/**
	 * Get unique identifier of device
	 * @return id
	 */
	public String getId() {
		return mNetwork.address + String.valueOf(getType());
	}

	/**
	 * Get name of device
	 * @return name
	 */
	public String getName() {
		return mName.length() > 0 ? mName : getId();
	}
		
	/**
	 * Setting name of device
	 * @param name
	 */
	public void setName(String name) {		
		mName = name;
	}
	
	/**
	 * Get location of device
	 * @return location
	 */
	public String getLocationId() {
		return mLocationId;
	}
	
	/**
	 * Setting location of device
	 * @param locationId
	 */
	public void setLocationId(String locationId) {
		mLocationId = locationId;
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
	
	/**
	 * Get visibility of device
	 * @return true if visible
	 */
	public boolean getVisibility() {
		return mVisibility;
	}
	
	/**
	 * Setting visibility of device
	 * @param visibility true if visible
	 */
	public void setVisibility(boolean visibility) {
		mVisibility = visibility;
	}
	
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

	@Override
	public String toString() {
		Location location = new Location(mLocationId, mLocationId, 0); // FIXME: get this location somehow from parent adapter
		
		return String.format("%s (%s)", getName(), location.getName());
	}
	
	/**
	 * Debug method
	 * @return
	 */
	public String toDebugString() {
		return String.format("Name: %s\nLocation: %s\nVisibility: %s\nInitialized: %s\nBattery: %s\nLogging: %s\nRefresh: %s\nValue: %s",
			mName, mLocationId, Boolean.toString(mVisibility), mInitialized, mBattery, mLogging, mRefreshInterval.getInterval(), getStringValue());
	}

	public boolean needsUpdate() {
		Time that = new Time();
		that.setToNow();
		that.set(that.toMillis(true) - mRefreshInterval.getInterval() * 1000); // x seconds interval between updates

		return lastUpdate.before(that);
	}

	/**
	 * Replace all data of this device by data of different device 
	 * @param newDevice with data that should be copied
	 */
	public void replaceData(BaseDevice newDevice) {
		setAddress(newDevice.getAddress());
		setBattery(newDevice.getBattery());
		setInitialized(newDevice.isInitialized());
		setInvolveTime(newDevice.getInvolveTime());
		setLocationId(newDevice.getLocationId());
		setLogging(newDevice.isLogging());
		setName(newDevice.getName());
		setQuality(newDevice.getQuality());
		setRefresh(newDevice.getRefresh());
		setValue(newDevice.getStringValue());
		setVisibility(newDevice.getVisibility());
		
		lastUpdate.set(newDevice.lastUpdate);
	}

}
