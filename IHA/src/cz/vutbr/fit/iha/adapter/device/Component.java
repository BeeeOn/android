/**
 * @brief Package for Devices that implements sensors
 */
package cz.vutbr.fit.iha.adapter.device;

import java.util.ArrayList;
import java.util.List;

import android.text.format.Time;

public class Component {
	protected boolean mInitialized;
	protected String mLocationId;
	protected RefreshInterval mRefreshInterval;	
	protected int mBattery;
	protected String mInvolveTime = "";

	protected NetworkState mNetwork = new NetworkState();
	
	public final Time lastUpdate = new Time();
	
	public final List<BaseDevice> devices = new ArrayList<BaseDevice>();
	
	/**
	 * Class constructor
	 */
	public Component() {}
	
	/**
	 * Public class that implements structure
	 */
	public final class NetworkState {
		public int quality;
		public String address = ""; 
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
		return mNetwork.address;
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

	public boolean needsUpdate() {
		Time that = new Time();
		that.setToNow();
		that.set(that.toMillis(true) - mRefreshInterval.getInterval() * 1000); // x seconds interval between updates

		return lastUpdate.before(that);
	}

}
