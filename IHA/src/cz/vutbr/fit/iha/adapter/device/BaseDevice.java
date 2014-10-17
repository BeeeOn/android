/**
 * @brief Package for Devices that implements sensors
 */
package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.adapter.device.values.BaseDeviceValue;

/**
 * @brief Abstract class for all devices
 * @author Robyer
 */
public abstract class BaseDevice {
	public static final String ID_SEPARATOR = "---";

	protected Facility mFacility;
	protected String mName = "";
	protected boolean mLogging;
	protected BaseDeviceValue mValue;

	/**
	 * Class constructor
	 */
	public BaseDevice() {
	}

	/**
	 * Represents settings of device which could be saved to server
	 */
	public enum SaveDevice {
		SAVE_NAME, // change name of device
		SAVE_LOCATION, // change location of facility
		SAVE_VISIBILITY, // change visibility of device
		SAVE_LOGGING, // change logging of device
		SAVE_REFRESH, // change refresh interval of facility
		SAVE_VALUE, // change value of actor device
	}

	/**
	 * Get numeric identifier representing type of this device
	 * 
	 * @return
	 */
	public abstract int getType();

	/**
	 * Get resource for human readable string representing type of this device
	 * 
	 * @return
	 */
	public abstract int getTypeStringResource();

	/**
	 * Get resource for icon representing type of this device
	 * 
	 * @return
	 */
	public abstract int getTypeIconResource();


	public void setFacility(Facility facility) {
		mFacility = facility;
	}

	public Facility getFacility() {
		return mFacility;
	}
	
	public void setValue(BaseDeviceValue value) {
		mValue = value;
	}
	
	public BaseDeviceValue getValue() {
		return mValue;
	}

	/**
	 * Get unique identifier of device
	 * 
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
	 * 
	 * @return name
	 */
	public String getName() {
		return mName.length() > 0 ? mName : getId();
	}

	/**
	 * Setting name of device
	 * 
	 * @param name
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * Returning true if there is some logging file for device
	 * 
	 * @return
	 */
	public boolean isLogging() {
		return mLogging;
	}

	/**
	 * Setting flag if there is logging file
	 * 
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
	 * 
	 * @return
	 */
	public String toDebugString() {
		return String.format("Name: %s\nLogging: %s\nValue: %s", mName, mLogging, getValue());
	}

	/**
	 * Replace all data of this device by data of different device
	 * 
	 * @param newDevice
	 *            with data that should be copied
	 */
	public void replaceData(BaseDevice newDevice) {
		setFacility(newDevice.getFacility());
		setLogging(newDevice.isLogging());
		setName(newDevice.getName());
		setValue(newDevice.getValue());
	}

}
