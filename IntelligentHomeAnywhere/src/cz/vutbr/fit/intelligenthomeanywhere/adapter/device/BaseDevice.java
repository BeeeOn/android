/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

/**
 * @author Robyer
 *
 */
public abstract class BaseDevice {
	protected boolean mInitialized;
	protected String mLocation;
	protected String mName;
	protected int mRefreshTime;	
	protected int mBattery;
	protected String mLog;
	protected boolean mLogging;
	protected String mInvolveTime;
	protected String mValue;
	
	protected NetworkState mNetwork = new NetworkState();
			
	/**
	 * Class constructor
	 */
	public BaseDevice() {}
	
	public final class NetworkState {
		public int quality;
		public String address; 
	}
	
	public abstract int getType();
	public abstract int getTypeString();
	
	public int getRefresh() {
		return mRefreshTime;
	}
		
	public void setRefresh(int time) {
		mRefreshTime = time;
	}
	
	public String getName() {
		return mName;
	}
		
	public void setName(String name) {		
		mName = name;
	}
	
	public String getLocation() {
		return mLocation;
	}
	
	public void setLocation(String location) {		
		mLocation = location;
	}
	
	public boolean isLogging() {
		return mLogging;
	}

	public void setLogging(boolean logging) {
		mLogging = logging;
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String value) {
		mValue = value;
	}
	
	public boolean isInitialized() {
		return mInitialized;
	}
	
	public void setInitialized(boolean initialized) {
		mInitialized = initialized;
	}
	
	public int getBattery() {
		return mBattery;
	}

	public void setBattery(int battery) {
		mBattery = battery;
	}

	public String getInvolveTime() {
		return mInvolveTime;
	}

	public void setInvolveTime(String involved_) {
		mInvolveTime = involved_;		
	}

	public String getAddress() {
		return mNetwork.address;
	}

	public void setAddress(String address) {
		mNetwork.address = address;
	}

	public int getQuality() {
		return mNetwork.quality;
	}

	public void setQuality(int quality) {
		mNetwork.quality = quality;
	}
	
	public String getLog() {
		return mLog;
	}
	
	public void setLog(String log) {
		mLog = log;
	}
	
	public String getStringType(){
		return "0x" + Integer.toHexString(getType()); 
	}

	public boolean saveSettings() {
		// TODO: save settings to server
		return true;
	}

}
