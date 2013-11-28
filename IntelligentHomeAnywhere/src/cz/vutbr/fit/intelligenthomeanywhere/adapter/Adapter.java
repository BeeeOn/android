/**
 * @brief Package for adapter manipulation
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlCreator;

/**
 * @brief Class for parsed data from XML file of adapters
 * @author ThinkDeep
 *
 */
public class Adapter {
	/**
	 * List of devices
	 */
	public List<BaseDevice> devices;
	private String mId;
	private String mVersion;
	private boolean mNewInit;
	private boolean mNewLocationName;
	private boolean mNewDeviceName;
	private String mNewDeviceNameLabel;
	
	public Adapter() {
		devices = new ArrayList<BaseDevice>();
		mId = null;
		mVersion = null;
		mNewInit = false;
		mNewLocationName = false;
		mNewDeviceName = false;
		mNewDeviceNameLabel = null;
	}
	
	/**
	 * Debug method
	 */
	public String toString(){
		String result = "";
		
		result += "ID is " + mId + "\n";
		result += "VERSION is " + mVersion + "\n";
		result += "___start of sensors___\n";
		
		for(BaseDevice dev : devices){
			result += dev.toString();
			result += "__\n";
		}
		
		return result;
	}
	
	/**
	 * Method for getting names of some location
	 * @param location name of group of devices
	 * @return list with string with name of device
	 */
	public List<String> getNameByLocation(String location){
		List<String> result = new ArrayList<String>();
		
		for(BaseDevice d : devices){
			if(d.getLocation() != null && d.getLocation().equals(location))
				result.add(d.getName());
		}
		
		return result;
	}
	
	/**
	 * Setting id of adapter
	 * @param ID
	 */
	public void setId(String ID){
		mId = ID;
	}
	
	/**
	 * Returning id of adapter
	 * @return id
	 */
	public String getId(){
		return mId;
	}
	
	/**
	 * Setting version of protocol
	 * @param Version
	 */
	public void setVersion(String Version){
		mVersion = Version;
	}
	
	/**
	 * Returning version of protocol
	 * @return version
	 */
	public String getVersion(){
		return mVersion;
	}
	
	/**
	 * Method checked if is there new device in network
	 * @return true if there is new, otherwise false
	 */
	public boolean isNewOne(){
		for(BaseDevice d : devices)
			if(!d.isInitialized())
				return true;
		return false;
	}
	
	/**
	 * Method return uninitialized adapter (new sensor in home)
	 * @return new adapter or null
	 */
	public BaseDevice getNewOne(){
		for(BaseDevice d : devices)
			if(!d.isInitialized())
				return d;
		return null;
	}
	
	/**
	 * Method search for actuator or sensor by name
	 * @param name of adapter (sensor/actuator)
	 * @return BaseDevice with found sensor/actuator or null
	 */
	public BaseDevice getDeviceByName(String name){
		for(BaseDevice d : devices)
			if(d.getName() != null && d.getName().equals(name))
				return d;
		return null;
	}
	
	/**
	 * Method for getting list of locations (first occurrence)
	 * @param notnull determines if it can return null value, or if it exclude
	 * @return ArayList of strings with unique locations
	 */
	public List<String> getLocations(boolean notnull){
		List<String> locations = new ArrayList<String>();
		
		for(BaseDevice d : devices){
			if(!locations.contains(d.getLocation())){
				if(notnull)
					if(d.getLocation() == null)
						continue;
				locations.add(d.getLocation());
			}
		}
		
		return locations;
	}
	
	/**
	 * Check if has been initialized new device, and set it to false
	 * @return true if there is new initialized device
	 */
	public boolean isNewInit(){
		if(mNewInit){
			mNewInit = false;
			return true;
		}
		return mNewInit;
	}
	
	/**
	 * Setting flag that there is new initialization
	 */
	public void setNewInit(){
		mNewInit = true;
	}
	
	/**
	 * Check if has been change name of location buttons
	 * @return true if is new name of location in otherwise return false
	 */
	public boolean isNewLocationName(){
		if(mNewLocationName){
			mNewLocationName = false;
			return true;
		}
		return false;
	}
	public void setNewLocationName(){
		mNewLocationName = true;
	}
	
	/**
	 * Check if has been change name of sensor
	 * @return true if change has been done, otherwise false
	 */
	public boolean isNewDeviceName(){
		if(mNewDeviceName){
			mNewDeviceName = false;
			return true;
		}
		return false;
	}
	
	/**
	 * Setting device name
	 * @param newName
	 */
	public void setNewDeviceName(String newName){
		mNewDeviceNameLabel = newName;
		mNewDeviceName = true;
	}
	
	/**
	 * Returning device name
	 * @return device name
	 */
	public String getNewDeviceName(){
		String result = mNewDeviceNameLabel;
		mNewDeviceNameLabel = null;
		return result;
	}
	
	/**
	 * Return object as XML file
	 * @return created XML string
	 */
	public String getXml(){
		XmlCreator xmlcreator = new XmlCreator(this);
		return xmlcreator.create();
	}
	
	/**
	 * Method for search all adapters by location
	 * @param name of location
	 * @return ArrayList with all adapters with needed location
	 */
	public List<BaseDevice> getDevicesByLocation(String name){
		List<BaseDevice> result = new ArrayList<BaseDevice>();
		
		for(BaseDevice d : devices){
			if(d.getLocation().equals(name))
				result.add(d);
		}
		
		return result;
	}
}
