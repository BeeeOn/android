package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

/**
 * Interface for flexible changing between temperature, switch, etc
 * @author ThinkDeep
 *
 */
public interface DeviceDestiny{
	
	public String getValue();
	public void setValue(String value);
	
	public void setLog(boolean bool);
	public boolean getLog();
	
	public String toString();
	
}
