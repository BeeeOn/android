/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

/**
 * @author Robyer
 *
 */
public abstract class BaseDevice {
	protected boolean _initialized;
	protected String _location;
	protected String _name;
	protected int _refreshTime;	
	protected int _battery;
	protected String _log;
	protected boolean _logging;
	protected String _involveTime;
	protected String _value;
	
	protected NetworkState _network = new NetworkState();
			
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
		return _refreshTime;
	}
		
	public void setRefresh(int time) {
		_refreshTime = time;
	}
	
	public String getName() {
		return _name;
	}
		
	public void setName(String name) {		
		_name = name;
	}
	
	public String getLocation() {
		return _location;
	}
	
	public void setLocation(String location) {		
		_location = location;
	}
	
	public boolean isLogging() {
		return _logging;
	}

	public void setLogging(boolean logging) {
		_logging = logging;
	}

	public String getValue() {
		return _value;
	}

	public void setValue(String value) {
		_value = value;
	}
	
	public boolean isInitialized() {
		return _initialized;
	}
	
	public void setInitialized(boolean initialized) {
		_initialized = initialized;
	}
	
	public int getBattery() {
		return _battery;
	}

	public void setBattery(int battery) {
		_battery = battery;
	}

	public String getInvolveTime() {
		return _involveTime;
	}

	public void setInvolveTime(String involved_) {
		_involveTime = involved_;		
	}

	public String getAddress() {
		return _network.address;
	}

	public void setAddress(String address) {
		_network.address = address;
	}

	public int getQuality() {
		return _network.quality;
	}

	public void setQuality(int quality) {
		_network.quality = quality;
	}
	
	public String getLog() {
		return _log;
	}
	
	public void setLog(String log) {
		_log = log;
	}
	
	public String getStringType(){
		return "0x" + Integer.toHexString(getType()); 
	}

	public boolean saveSettings() {
		// TODO: save settings to server
		return true;
	}

}
