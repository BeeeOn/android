/**
 * @brief Package for Devices that implements sensors
 */
package cz.vutbr.fit.iha.adapter.device;

import android.content.Context;

/**
 * @brief Abstract class for all devices
 * @author Robyer
 */
public abstract class BaseDevice {
	public static final String ID_SEPARATOR = "---";
	
	protected Facility mFacility;
	protected String mName = "";
	protected boolean mLogging;
	
	/**
	 * Class constructor
	 */
	public BaseDevice() {
	}
	
	/**
	 * Represents settings of device which could be saved to server
	 */
	// FIXME: only name, logging, "type" (icon) and value can be changed here 
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
	
	public void setFacility(Facility facility) {
		mFacility = facility;
	}
	
	public Facility getFacility() {
		return mFacility;
	}
	
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
	 * Get unique identifier of device
	 * @return id
	 */
	public String getId() {
		// TODO: what if there will be more devices with same type for one facility?
		if (mFacility == null)
			throw new RuntimeException("Device's facility is NULL, WHY!?");
		
		return mFacility.getAddress() + ID_SEPARATOR + String.valueOf(getType());
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
	
	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * Debug method
	 * @return
	 */
	public String toDebugString() {
		return String.format("Name: %s\nLogging: %s\nValue: %s", mName, mLogging, getStringValue());
	}

	/**
	 * Replace all data of this device by data of different device 
	 * @param newDevice with data that should be copied
	 */
	public void replaceData(BaseDevice newDevice) {
		setFacility(newDevice.getFacility());
		setLogging(newDevice.isLogging());
		setName(newDevice.getName());
		setValue(newDevice.getStringValue());
	}

}
