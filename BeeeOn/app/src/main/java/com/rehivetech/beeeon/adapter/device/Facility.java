/**
 * @brief Package for facilitys that implements sensors
 */
package com.rehivetech.beeeon.adapter.device;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.NameIdentifierComparator;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @brief Facility class which contains own data and devices (sensors, actors)
 * @author Robyer
 */
public class Facility implements IIdentifier {
	protected String mAddress;
	protected String mAdapterId;
	protected String mLocationId;
	protected boolean mInitialized;
	protected RefreshInterval mRefreshInterval;
	protected int mBattery;
	protected DateTime mInvolveTime;
	protected int mNetworkQuality;
	protected DateTime mLastUpdate;
	protected final List<Device> mDevices = new ArrayList<Device>();

	private boolean mSorted; // optimization to sort values only when needed

	/**
	 * Class constructor
	 */
	public Facility() {
	}

	/**
	 * Represents settings of facility which could be saved to server
	 */
	public enum Savefacility {
		SAVE_NAME, // rename facility
		SAVE_LOCATION, // change location
		SAVE_VISIBILITY, // change visibility //NOTE: sending always
		SAVE_REFRESH, // change refresh interval
		SAVE_VALUE, // change value (of actor)
		SAVE_TYPE, // change facility's icon, etc. //NOTE: what? type cannot be changed
	}

	/**
	 * Get last update time
	 * 
	 * @return
	 */
	public DateTime getLastUpdate() {
		return mLastUpdate;
	}

	/**
	 * Setting last update time
	 * 
	 * @param lastUpdate
	 */
	public void setLastUpdate(DateTime lastUpdate) {
		mLastUpdate = lastUpdate;
	}

	/**
	 * Check if actual value of this sensor is expired
	 * 
	 * @return true when refresh interval since last update expired
	 */
	public boolean isExpired() {
		return mLastUpdate.plusSeconds(mRefreshInterval.getInterval()).isBeforeNow();
	}

	/**
	 * Get refresh interval
	 * 
	 * @return refresh interval
	 */
	public RefreshInterval getRefresh() {
		return mRefreshInterval;
	}

	/**
	 * Setting refresh interval
	 * 
	 * @param interval
	 */
	public void setRefresh(RefreshInterval interval) {
		mRefreshInterval = interval;
	}

	/**
	 * Get unique identifier of facility
	 * 
	 * @return id
	 */
	public String getId() {
		return mAddress;
	}

	/**
	 * Get location of facility
	 * 
	 * @return location
	 */
	public String getLocationId() {
		return mLocationId;
	}

	/**
	 * Setting location of facility
	 * 
	 * @param locationId
	 */
	public void setLocationId(String locationId) {
		mLocationId = locationId;
	}

	/**
	 * Get adapter id of facility
	 * 
	 * @return adapter id
	 */
	public String getAdapterId() {
		return mAdapterId;
	}

	/**
	 * Setting adapter id of facility
	 * 
	 * @param adapterId
	 */
	public void setAdapterId(String adapterId) {
		mAdapterId = adapterId;
	}

	/**
	 * Returning flag if facility has been initialized yet
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * Setting flag for facility initialization state
	 * 
	 * @param initialized
	 */
	public void setInitialized(boolean initialized) {
		mInitialized = initialized;
	}

	/**
	 * Get state of battery
	 * 
	 * @return battery
	 */
	public int getBattery() {
		return mBattery;
	}

	/**
	 * Setting state of battery
	 * 
	 * @param battery
	 */
	public void setBattery(int battery) {
		mBattery = battery;
	}

	/**
	 * Get time of setting of facility to system
	 * 
	 * @return involve time
	 */
	public DateTime getInvolveTime() {
		return mInvolveTime;
	}

	/**
	 * Setting involve time
	 * 
	 * @param involved
	 */
	public void setInvolveTime(DateTime involved) {
		mInvolveTime = involved;
	}

	/**
	 * Get MAC address of facility
	 * 
	 * @return address
	 */
	public String getAddress() {
		return mAddress;
	}

	/**
	 * Setting MAC address
	 * 
	 * @param address
	 */
	public void setAddress(String address) {
		mAddress = address;
	}

	/**
	 * Get value of signal quality
	 * 
	 * @return quality
	 */
	public int getNetworkQuality() {
		return mNetworkQuality;
	}

	/**
	 * Setting quality
	 * 
	 * @param networkQuality
	 */
	public void setNetworkQuality(int networkQuality) {
		mNetworkQuality = networkQuality;
	}

	@Override
	public String toString() {
		return String.format("Facility: %s", getId());
	}

	/**
	 * Debug method
	 * 
	 * @return
	 */
	public String toDebugString() {
		return String.format("Id: %s\nAdapter: %s\nLocation: %s\nInitialized: %s\nBattery: %s\nRefresh: %s\nDevices: %s", getId(), mAdapterId, mLocationId, mInitialized, mBattery,
				mRefreshInterval.getInterval(), Integer.toString(mDevices.size()));
	}

	public void addDevice(Device device) {
		device.setFacility(this);
		mDevices.add(device);
		mSorted = false;
	}

	public void clearDevices() {
		mDevices.clear();
	}

	public List<Device> getDevices() {
		if (!mSorted) {
			mSorted = true;
			// Sort devices by id (= by type)
			Collections.sort(mDevices, new NameIdentifierComparator());
		}

		return mDevices;
	}

	public Device getDeviceByType(DeviceType type, int offset) {
		for (Device device : getDevices()) {
			if (device.getType().equals(type) && device.getOffset() == offset) {
				return device;
			}
		}

		return null;
	}

}
