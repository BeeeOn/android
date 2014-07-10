/**
 * @brief Package for Devices that implements sensors
 */
package cz.vutbr.fit.iha.adapter.device;

import android.content.Context;
import android.text.format.Time;
import cz.vutbr.fit.iha.adapter.location.Location;

/**
 * @brief Abstract class for all devices
 * @author Robyer
 */
public abstract class BaseDevice {
	protected boolean mInitialized;
	protected String mLocationId;
	protected String mName = "";
	protected int mRefreshTime;	
	protected int mBattery;
	protected boolean mLogging;
	protected String mInvolveTime = "";
	protected VisibilityState mVisibility;
	
	protected NetworkState mNetwork = new NetworkState();
	
	public final Time lastUpdate = new Time();
			
	/**
	 * Class constructor
	 */
	public BaseDevice() {}
	
	/**
	 * Public class that implements structure
	 */
	public final class NetworkState {
		public int quality;
		public String address = ""; 
	}
	
	/**
	 * Represents settings of device which could be saved to server.
	 */
	public enum SaveDevice {
		SAVE_ALL,			// save all settings
		SAVE_NAME,			// rename device
		SAVE_LOCATION,		// change location
		SAVE_VISIBILITY,	// change visibility
		SAVE_LOGGING,		// change logging on server
		SAVE_REFRESH,		// change refresh interval
		SAVE_TYPE,			// change device's icon, etc.
	}
	
	/**
	 * Represents visibility state of device 
	 * @author ThinkDeep
	 *
	 */
	public enum VisibilityState {
		VISIBLE,
		HIDDEN,
		DELETE
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
	 * Get refresh time in secs
	 * @return refresh time (secs)
	 */
	public int getRefresh() {
		return mRefreshTime;
	}
		
	/**
	 * Setting refresh time in secs
	 * @param secs
	 */
	public void setRefresh(int secs) {
		mRefreshTime = secs;
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
	 * @return visibility
	 */
	public char getVisibilityChar(){
		return getVisibilityFromState(mVisibility);
	}
	
	/**
	 * Set visibility of device
	 * @param visibility
	 */
	public void setVisibility(char visibility){
		mVisibility = getVisibilityFromChar(visibility);
	}
	
	public void setVisibility(VisibilityState state){
		mVisibility = state;
	}
	
	public VisibilityState getVisibility(){
		return mVisibility;
	}
	
	/**
	 * Method parse char to visibitilyState
	 * @param visibility
	 * @return VisibilityState by char or HIDDEN
	 */
	public static VisibilityState getVisibilityFromChar(char visibility){
		switch(visibility){
			case 'i':
				return VisibilityState.VISIBLE;
			case 'o':
				return VisibilityState.HIDDEN;
			case 'x':
				return VisibilityState.DELETE;
			default:
				return VisibilityState.HIDDEN;
		}
	}
	
	public static char getVisibilityFromState(VisibilityState visibility){
		switch(visibility){
		case DELETE:
			return 'x';
		case HIDDEN:
			return 'o';
		case VISIBLE:
			return 'i';
		default:
			return 'o';
		}
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
		String result = "";

		result += "Name: " + mName + "\n";
		result += "Location: " + mLocationId + "\n";
		result += "Visibility: " + mVisibility + "\n";
		result += "Initialized: " + mInitialized + "\n";
		result += "Battery: " + mBattery + "\n";
		result += "Logging: " + mLogging + "\n";
		result += "Value: " + getStringValue() + "\n";
		
		return result;
	}

	public boolean needsUpdate() {
		Time that = new Time();
		that.setToNow();
		that.set(that.toMillis(true) - getRefresh() * 1000); // x seconds interval between updates

		return lastUpdate.before(that);
	}

}
