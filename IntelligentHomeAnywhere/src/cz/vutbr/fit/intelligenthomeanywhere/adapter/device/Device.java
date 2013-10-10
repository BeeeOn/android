package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;

/**
 * Class for parsed data from sensors
 * @author ThinkDeep
 */
public class Device implements Adapter{

	private int _type;
	private String _location;
	private String _name;
	private int _refreshTime;
	private boolean _initialized;
	private int _battery;
	private String _address;
	private int _quality;
	private String _Log;
	private String _involveTime;
	public DeviceDestiny deviceDestiny;
	
	public Device(){
		_location = null;
		_name = null;
		_refreshTime = 0;
		_initialized = false;
		_battery = 0;
		_address = null;
		_quality = 0;
		_Log = null;
		deviceDestiny = null;
		_type = 0;
		_involveTime = null;
	};
	
	public String toString(){
		String result = "";
		
		result += Integer.toString(_type) + "\n";
		result += _location + "\n";
		result += _name + "\n";
		result += Integer.toString(_refreshTime) + "\n";
		result += Integer.toString(_battery) + "\n";
		result += _address + "\n";
		result += Integer.toString(_quality) + "\n";
		result += deviceDestiny.toString() + "\n";
		result += _Log + "\n";
		
		return result;
	}
	
	public void setLocation(String location){
		_location = location;
	}
	public String getLocation(){
		return _location;
	}
	
	public void setInit(boolean init){
		_initialized = init;
	}
	public boolean getInit(){
		return _initialized;
	}
	
	public void setName(String name){
		_name = name;
	}
	public String getName(){
		return _name;
	}
	
	public void setRefresh(int refresh){
		_refreshTime = refresh;
	}
	public int getRefresh(){
		return _refreshTime;
	}
	
	public void setBattery(int battery){
		_battery = battery;
	}
	public int getBattery(){
		return _battery;
	}
	
	public void setAddress(String address){
		_address = address;
	}
	public String getAddress(){
		return _address;
	}
	
	public void setQuality(int quality){
		_quality = quality;
	}
	public int getQuality(){
		return _quality;
	}

	public void setLog(String log){
		_Log = log;
	}
	public String getLog(){
		return _Log;
	}

	public void setType(int type){
		_type = type;
	}
	public int getType(){
		return _type;
	}
	public String getStringType(){
		String result = "0x";
		result += Integer.toHexString(_type);
		return result;
	}


	public void setInvolveTime(String time) {
		_involveTime = time;
	}
	public String getInvolveTime() {
		return _involveTime;
	}
}
