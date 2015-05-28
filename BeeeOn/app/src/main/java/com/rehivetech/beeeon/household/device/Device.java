/**
 * @brief Package for devices that implements sensors
 */
package com.rehivetech.beeeon.household.device;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.household.location.Location;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Robyer
 * @brief Device class which contains own data and devices (sensors, actors)
 */
public class Device implements IIdentifier {
	protected String mAddress;
	protected String mGateId;
	protected String mLocationId;
	protected boolean mInitialized;
	protected RefreshInterval mRefreshInterval;
	protected int mBattery;
	protected DateTime mInvolveTime;
	protected int mNetworkQuality;
	protected DateTime mLastUpdate;
	protected final List<Module> mModules = new ArrayList<Module>();

	private boolean mSorted; // optimization to sort values only when needed

	/**
	 * Class constructor
	 */
	public Device() {
	}

	/**
	 * Represents settings of mDevice which could be saved to server
	 */
	public enum SaveDevice {
		SAVE_NAME, // rename mDevice
		SAVE_LOCATION, // change location
		SAVE_VISIBILITY, // change visibility //NOTE: sending always
		SAVE_REFRESH, // change refresh interval
		SAVE_VALUE, // change value (of actor)
		SAVE_TYPE, // change mDevice's icon, etc. //NOTE: what? type cannot be changed
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
	 * Get unique identifier of mDevice
	 *
	 * @return id
	 */
	public String getId() {
		return mAddress;
	}

	/**
	 * Get location of mDevice
	 *
	 * @return location
	 */
	public String getLocationId() {
		return mLocationId;
	}

	/**
	 * Setting location of mDevice
	 *
	 * @param locationId
	 */
	public void setLocationId(String locationId) {
		// From server we've got "", but internally we need to use Location.NO_LOCATION_ID
		if (locationId.isEmpty())
			locationId = Location.NO_LOCATION_ID;

		mLocationId = locationId;
	}

	/**
	 * Get gate id of mDevice
	 *
	 * @return gate id
	 */
	public String getGateId() {
		return mGateId;
	}

	/**
	 * Setting gate id of mDevice
	 *
	 * @param gateId
	 */
	public void setGateId(String gateId) {
		mGateId = gateId;
	}

	/**
	 * Returning flag if mDevice has been initialized yet
	 *
	 * @return
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * Setting flag for mDevice initialization state
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
	 * Get time of setting of mDevice to system
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
	 * Get MAC address of mDevice
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
		return String.format("Device: %s", getId());
	}

	/**
	 * Debug method
	 *
	 * @return
	 */
	public String toDebugString() {
		return String.format("Id: %s\nGate: %s\nLocation: %s\nInitialized: %s\nBattery: %s\nRefresh: %s\nDevices: %s", getId(), mGateId, mLocationId, mInitialized, mBattery,
				mRefreshInterval.getInterval(), Integer.toString(mModules.size()));
	}

	public void addModule(Module module) {
		module.setDevice(this);
		mModules.add(module);
		mSorted = false;
	}

	public void clearModules() {
		mModules.clear();
	}

	public List<Module> getModules() {
		if (!mSorted) {
			mSorted = true;
			// Sort devices by offset (= by id, which is combined from mac address + raw type, where type is type id + offset)
			Collections.sort(mModules, new IdentifierComparator());
		}

		return mModules;
	}

	public Module getModuleByType(ModuleType type, int offset) {
		for (Module module : getModules()) {
			if (module.getType().equals(type) && module.getOffset() == offset) {
				return module;
			}
		}

		return null;
	}

}
