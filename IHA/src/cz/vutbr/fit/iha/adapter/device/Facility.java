/**
 * @brief Package for facilitys that implements sensors
 */
package cz.vutbr.fit.iha.adapter.device;

import java.util.ArrayList;
import java.util.List;

import android.text.format.Time;

/**
 * @brief Facility class which contains own data and devices (sensors, actors)
 * @author Robyer
 */
public class Facility {
	protected boolean mInitialized;
	protected String mLocationId;
	protected RefreshInterval mRefreshInterval;	
	protected int mBattery;
	protected boolean mLogging;
	protected String mInvolveTime = "";
	protected boolean mVisibility;
	
	protected NetworkState mNetwork = new NetworkState();
	
	public final Time lastUpdate = new Time();
	
	protected final List<BaseDevice> mDevices = new ArrayList<BaseDevice>();
	
	protected String mAdapterId;
	
	/**
	 * Class constructor
	 */
	public Facility() {
	}
	
	/**
	 * Public class that implements structure
	 */
	public final class NetworkState {
		public int quality;
		public String address = ""; 
	}
	
	/**
	 * Represents settings of facility which could be saved to server
	 */
	public enum Savefacility {
		SAVE_NAME,			// rename facility
		SAVE_LOCATION,		// change location
		SAVE_VISIBILITY,	// change visibility				//NOTE: sending always
		SAVE_LOGGING,		// change logging on server
		SAVE_REFRESH,		// change refresh interval
		SAVE_VALUE,			// change value (of actor)
		SAVE_TYPE,			// change facility's icon, etc.		//NOTE: what? type cannot be changed
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
	 * Get unique identifier of facility
	 * @return id
	 */
	public String getId() {
		return mNetwork.address;
	}

	/**
	 * Get location of facility
	 * @return location
	 */
	public String getLocationId() {
		return mLocationId;
	}
	
	/**
	 * Setting location of facility
	 * @param locationId
	 */
	public void setLocationId(String locationId) {
		mLocationId = locationId;
	}
	
	/**
	 * Get adapter id of facility
	 * @return adapter id
	 */
	public String getAdapterId() {
		return mAdapterId;
	}
	
	/**
	 * Setting adapter id of facility
	 * @param adapterId
	 */
	public void setAdapterId(String adapterId) {
		mAdapterId = adapterId;
	}
	
	/**
	 * Returning true if there is some logging file for facility
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
	 * Get visibility of facility
	 * @return true if visible
	 */
	public boolean getVisibility() {
		return mVisibility;
	}
	
	/**
	 * Setting visibility of facility
	 * @param visibility true if visible
	 */
	public void setVisibility(boolean visibility) {
		mVisibility = visibility;
	}
	
	/**
	 * Returning flag if facility has been initialized yet
	 * @return
	 */
	public boolean isInitialized() {
		return mInitialized;
	}
	
	/**
	 * Setting flag for facility initialization state
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
	 * Get time of setting of facility to system
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
	 * Get MAC address of facility
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
		return String.format("Facility: %s", getId());
	}
	
	/**
	 * Debug method
	 * @return
	 */
	public String toDebugString() {
		return String.format("Id: %s\nLocation: %s\nVisibility: %s\nInitialized: %s\nBattery: %s\nLogging: %s\nRefresh: %s\nDevices: %s",
			getId(), mLocationId, Boolean.toString(mVisibility), mInitialized, mBattery, mLogging, mRefreshInterval.getInterval(), Integer.toString(mDevices.size()));
	}

	public boolean needsUpdate() {
		Time that = new Time();
		that.setToNow();
		that.set(that.toMillis(true) - mRefreshInterval.getInterval() * 1000); // x seconds interval between updates

		return lastUpdate.before(that);
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
	
	public BaseDevice getDeviceByType(int type) {
		for (BaseDevice device : getDevices()) {
			if (device.getType() == type) {
				return device;
			}
		}

		return null;
	}

	/**
	 * Replace all data of this facility by data of different facility 
	 * @param newFacility with data that should be copied
	 */
	public void replaceData(Facility newFacility) {
		setAddress(newFacility.getAddress());
		setBattery(newFacility.getBattery());
		setInitialized(newFacility.isInitialized());
		setInvolveTime(newFacility.getInvolveTime());
		setLocationId(newFacility.getLocationId());
		setLogging(newFacility.isLogging());
		setQuality(newFacility.getQuality());
		setRefresh(newFacility.getRefresh());
		setVisibility(newFacility.getVisibility());
		
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
		
		lastUpdate.set(newFacility.lastUpdate);
	}

}
