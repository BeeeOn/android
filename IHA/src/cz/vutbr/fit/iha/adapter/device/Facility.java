/**
 * @brief Package for facilitys that implements sensors
 */
package cz.vutbr.fit.iha.adapter.device;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

/**
 * @brief Facility class which contains own data and devices (sensors, actors)
 * @author Robyer
 */
public class Facility {
	protected String mAddress;
	protected String mAdapterId;
	protected String mLocationId;
	protected boolean mInitialized;	
	protected RefreshInterval mRefreshInterval;
	protected int mBattery;
	protected boolean mLogging;
	protected String mInvolveTime = "";
	protected boolean mVisibility;
	protected int mNetworkQuality;	
	protected DateTime mLastUpdate;
	protected final List<BaseDevice> mDevices = new ArrayList<BaseDevice>();

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
		SAVE_LOGGING, // change logging on server
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
	 * Returning true if there is some logging file for facility
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

	/**
	 * Get visibility of facility
	 * 
	 * @return true if visible
	 */
	public boolean getVisibility() {
		return mVisibility;
	}

	/**
	 * Setting visibility of facility
	 * 
	 * @param visibility
	 *            true if visible
	 */
	public void setVisibility(boolean visibility) {
		mVisibility = visibility;
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
	public String getInvolveTime() {
		return mInvolveTime;
	}

	/**
	 * Setting involve time
	 * 
	 * @param involved
	 */
	public void setInvolveTime(String involved) {
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
		return String.format("Id: %s\nAdapter: %s\nLocation: %s\nVisibility: %s\nInitialized: %s\nBattery: %s\nLogging: %s\nRefresh: %s\nDevices: %s", getId(), mAdapterId, mLocationId,
				Boolean.toString(mVisibility), mInitialized, mBattery, mLogging, mRefreshInterval.getInterval(), Integer.toString(mDevices.size()));
	}

	public void addDevice(BaseDevice device) {
		device.setFacility(this);
		mDevices.add(device);
	}

	public void clearDevices() {
		mDevices.clear();
	}

	public List<BaseDevice> getDevices() {
		return mDevices;
	}

	public BaseDevice getDeviceByType(DeviceType type) {
		for (BaseDevice device : getDevices()) {
			if (device.getType().equals(type)) {
				return device;
			}
		}

		return null;
	}

	/**
	 * Replace all data of this facility by data of different facility
	 * 
	 * @param newFacility
	 *            with data that should be copied
	 */
	public void replaceData(Facility newFacility) {
		setAdapterId(newFacility.getAdapterId());
		setAddress(newFacility.getAddress());
		setBattery(newFacility.getBattery());
		setInitialized(newFacility.isInitialized());
		setInvolveTime(newFacility.getInvolveTime());
		setLocationId(newFacility.getLocationId());
		setLogging(newFacility.isLogging());
		setNetworkQuality(newFacility.getNetworkQuality());
		setRefresh(newFacility.getRefresh());
		setVisibility(newFacility.getVisibility());
		setLastUpdate(newFacility.getLastUpdate());

		mDevices.clear();
		for (BaseDevice newDevice : newFacility.mDevices) {
			try {
				BaseDevice device = newDevice.getClass().newInstance();
				device.replaceData(newDevice);
				mDevices.add(device);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

}
