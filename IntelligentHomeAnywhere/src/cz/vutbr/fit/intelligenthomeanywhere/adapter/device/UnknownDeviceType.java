package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;


/**
 * Class that implements interface for devices
 * @author ThinkDeep
 *
 */
public class UnknownDeviceType implements DeviceDestiny {
	private String _value;
	private boolean _Log;
	
	public UnknownDeviceType(){}
	
	public void setValue(String value){
		_value = value;
	}
	public String getValue(){
		return _value;
	}
	
	public void setLog(boolean log){
		_Log = log;
	}
	public boolean getLog(){
		return _Log;
	}
	
	public String toString(){
		String result = "";
		result += _value + "\n";
		result += String.valueOf(_Log) + "\n";
		return result;
	}
	
}
