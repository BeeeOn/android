/**
 * @brief Package for Devices that implements sensors
 */
package com.rehivetech.beeeon.household.device;

import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.household.device.values.BaseValue;

/**
 * @brief Abstract class for all devices
 * @author Robyer
 */
public class Device implements INameIdentifier {
	public static final String ID_SEPARATOR = "---";

	protected Facility mFacility;
	protected String mName = "";
	protected boolean mVisibility;

	public final BaseValue mValue;

	public final DeviceType mType;
	public final String mRawTypeId;
	public final int mOffset;

	/**
	 * Class constructor
	 */
	public Device(DeviceType type, BaseValue value, String rawTypeId, int offset) {
		mType = type;
		mValue = value;
		mRawTypeId = rawTypeId;
		mOffset = offset;
	}

	public static Device createFromDeviceTypeId(String typeId) {
		int iType = -1; // unknown type
		int offset = 0; // default offset

		if (!typeId.isEmpty()) {
			// Get integer representation of the given string value
			int value = Integer.parseInt(typeId);

			// Separate combined value to type and offset
			iType = value % 256;
			offset = value / 256;
		}

		DeviceType type = DeviceType.fromTypeId(iType);
		BaseValue value = BaseValue.createFromDeviceType(type);

		// Create device object with DeviceType, BaseValue, original raw value of type, and offset
		return new Device(type, value, typeId, offset);
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
		return getType().isActor() ? mValue.getActorIconResource() : mValue.getIconResource();
	}

	public void setFacility(Facility facility) {
		mFacility = facility;
	}

	public Facility getFacility() {
		return mFacility;
	}

	/**
	 * Get unique identifier of device (address of facility + raw type id containing offset)
	 * 
	 * @return id
	 */
	public String getId() {
		if (mFacility == null)
			throw new RuntimeException("Device's facility is null!");

		return mFacility.getAddress() + ID_SEPARATOR + getRawTypeId();
	}

	public String getRawTypeId() {
		return mRawTypeId;
	}

	public int getOffset() {
		return mOffset;
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
	 * Get name of device in raw form server (it is for third-part sensors)
	 *
	 * @return name
	 */
	public String getServerName() {
		return mName.length() > 0 ? mName : "";
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
