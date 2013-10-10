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
