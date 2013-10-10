package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

/**
 * Interface for flexible changing between temperature, switch, etc
 * @author ThinkDeep
 *
 */
public interface DeviceDestiny{
	
	public String GetValue();
	public void SetValue(String value);
	
	public void SetLog(boolean bool);
	public boolean GetLog();
	
	public String toString();
	
}
