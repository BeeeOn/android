package cz.vutbr.fit.intelligenthomeanywhere;

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
	
	/**
	 * Method return name of class in lowercase
	 * @return name of class that implements this interface
	 */
	//public String GetClassName();
}
