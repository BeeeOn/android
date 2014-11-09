/**
 * @brief Package for Devices that implements sensors
 */
package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.IIdentifier;
import cz.vutbr.fit.iha.adapter.device.values.BaseValue;

/**
 * @brief Abstract class for all devices
 * @author Robyer
 */
public class Device implements IIdentifier {
	public static final String ID_SEPARATOR = "---";

	protected Facility mFacility;
	protected String mName = "";
	protected boolean mVisibility;

	public final DeviceType mType;
	public final BaseValue mValue;

	/**
	 * Class constructor
	 */
	public Device(DeviceType type, BaseValue value) {
		mType = type;
		mValue = value;
	}

	/**
	 * Represents settings of device which could be saved to server
	 */
	public enum SaveDevice {
		SAVE_NAME, // change name of device
		SAVE_LOCATION, // change location of facility
		SAVE_VISIBILITY, // change visibility of device
		SAVE_REFRESH, // change refresh interval of facility
		SAVE_VALUE, // change value of actor device
		SAVE_INITIALIZED,
	}

	public DeviceType getType() {
		return mType;
	}

	public BaseValue getValue() {
		return mValue;
	}

	public void setValue(String value) {
		mValue.setValue(value);
	}

	/**
	 * Get resource for human readable string representing type of this device
	 * 
	 * @return
	 */
	public int getTypeStringResource() {
		return mType.getStringResource();
	}

	public int getIconResource() {
		return mValue.getIconResource();
	}

	public void setFacility(Facility facility) {
		mFacility = facility;
	}

	public Facility getFacility() {
		return mFacility;
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

		return mFacility.getAddress() + ID_SEPARATOR + String.valueOf(mType.getTypeId());
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
	 * Get visibility of device
	 * 
	 * @return true if visible
	 */
	public boolean isVisible() {
		return mVisibility;
	}

	/**
	 * Setting visibility of device
	 * 
	 * @param visibility
	 *            true if visible
	 */
	public void setVisibility(boolean visibility) {
		mVisibility = visibility;
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
		return String.format("Name: %s\nVisibility: %s\nValue: %s", mName, Boolean.toString(mVisibility), mValue);
	}

}
