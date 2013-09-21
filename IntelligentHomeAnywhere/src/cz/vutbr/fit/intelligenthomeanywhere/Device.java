package cz.vutbr.fit.intelligenthomeanywhere;

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
	public DeviceDestiny deviceDestiny; 
	
	Device(){
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
	
	public void SetLocation(String location){
		_location = location;
	}
	public String GetLocation(){
		return _location;
	}
	
	public void SetInit(boolean init){
		_initialized = init;
	}
	public boolean GetInit(){
		return _initialized;
	}
	
	public void SetName(String name){
		_name = name;
	}
	public String GetName(){
		return _name;
	}
	
	public void SetRefresh(int refresh){
		_refreshTime = refresh;
	}
	public int GetRefresh(){
		return _refreshTime;
	}
	
	public void SetBattery(int battery){
		_battery = battery;
	}
	public int GetBattery(){
		return _battery;
	}
	
	public void SetAddress(String address){
		_address = address;
	}
	public String GetAddress(){
		return _address;
	}
	
	public void SetQuality(int quality){
		_quality = quality;
	}
	public int GetQuality(){
		return _quality;
	}

	public void SetLog(String log){
		_Log = log;
	}
	public String GetLog(){
		return _Log;
	}

	public void SetType(int type){
		_type = type;
	}
	public int GetType(){
		return _type;
	}
	public String GetStringType(){
		String result = "0x";
		result += Integer.toHexString(_type);
		return result;
	}
}
