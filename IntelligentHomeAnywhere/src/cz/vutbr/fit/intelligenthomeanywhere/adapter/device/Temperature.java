package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;


/**
 * Class that implements interface for devices
 * @author ThinkDeep
 *
 */
public class Temperature implements DeviceDestiny{
	private int _value;
	private boolean _Log;
	
	public Temperature(){}
	
	public void setValue(String value){
		_value = Integer.parseInt(value);
	}
	public String getValue(){
		return Integer.toString(_value);
	}
	
	public void setLog(boolean log){
		_Log = log;
	}
	public boolean getLog(){
		return _Log;
	}
	
	public String toString(){
		String result = "";
		result += Integer.toString(_value) + "\n";
		result += String.valueOf(_Log) + "\n";
		return result;
	}

}
