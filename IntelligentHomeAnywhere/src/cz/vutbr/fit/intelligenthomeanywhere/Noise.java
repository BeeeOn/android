package cz.vutbr.fit.intelligenthomeanywhere;

/**
 * Class that implements interface for devices
 * @author ThinkDeep
 *
 */
public class Noise implements DeviceDestiny{
	private int _value;
	private boolean _Log;
	
	Noise(){}
	
	public void SetValue(String value){
		_value = Integer.parseInt(value);
	}
	public String GetValue(){
		return Integer.toString(_value);
	}
	
	public void SetLog(boolean log){
		_Log = log;
	}
	public boolean GetLog(){
		return _Log;
	}
	
	public String toString(){
		String result = "";
		result += Integer.toString(_value) + "\n";
		result += String.valueOf(_Log) + "\n";
		return result;
	}

}
