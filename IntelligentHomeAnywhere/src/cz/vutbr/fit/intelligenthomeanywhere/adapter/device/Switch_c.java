package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;


/**
 * Class that implements interface for devices
 * @author ThinkDeep
 *
 */
public class Switch_c implements DeviceDestiny {
	private String _value;
	private boolean _Log;
	
	public Switch_c(){}
	
	public void SetValue(String value){
		_value = value;
	}
	public String GetValue(){
		return _value;
	}
	
	public void SetLog(boolean log){
		_Log = log;
	}
	public boolean GetLog(){
		return _Log;
	}
	
	public String toString(){
		String result = "";
		result += _value + "\n";
		result += String.valueOf(_Log) + "\n";
		return result;
	}
}
