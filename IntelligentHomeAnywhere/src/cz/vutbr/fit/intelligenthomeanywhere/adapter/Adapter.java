package cz.vutbr.fit.intelligenthomeanywhere.adapter;

import java.util.ArrayList;

import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlCreator;

/**
 * Class for parsed data from xml file of adapters
 * @author ThinkDeep
 *
 */
public class Adapter {
	public ArrayList<BaseDevice> devices;
	private String _id;
	private String _version;
	private boolean _newInit;
	private boolean _newLocationName;
	private boolean _newDeviceName;
	private String _newDeviceNameLabel;
	
	public Adapter() {
		devices = new ArrayList<BaseDevice>();
		_id = null;
		_version = null;
		_newInit = false;
		_newLocationName = false;
		_newDeviceName = false;
		_newDeviceNameLabel = null;
	}
	
	public String toString(){
		String result = "";
		
		result += "ID is " + _id + "\n";
		result += "VERSION is " + _version + "\n";
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
	public ArrayList<String> getNameByLocation(String location){
		ArrayList<String> result = new ArrayList<String>();
		
		for(BaseDevice d : devices){
			if(d.getLocation() != null && d.getLocation().equals(location))
				result.add(d.getName());
		}
		
		return result;
	}
	
	public void setId(String ID){
		_id = ID;
	}
	public String getId(){
		return _id;
	}
	
	public void setVersion(String Version){
		_version = Version;
	}
	public String getVersion(){
		return _version;
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
	 * Method for getting list of locations (first occurence)
	 * @param notnull determines if it can return null value, or if it exclude
	 * @return arraylist of strings with unique locations
	 */
	public ArrayList<String> getLocations(boolean notnull){
		ArrayList<String> locations = new ArrayList<String>();
		
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
		if(_newInit){
			_newInit = false;
			return true;
		}
		return _newInit;
	}
	public void setNewInit(){
		_newInit = true;
	}
	
	/**
	 * Check if has been change name of location buttons
	 * @return true if si new name of location in otherwise return false
	 */
	public boolean isNewLocationName(){
		if(_newLocationName){
			_newLocationName = false;
			return true;
		}
		return false;
	}
	public void setNewLocationName(){
		_newLocationName = true;
	}
	
	/**
	 * Check if has been change name of sensor
	 * @return true if change has been done, otherwise false
	 */
	public boolean isNewDeviceName(){
		if(_newDeviceName){
			_newDeviceName = false;
			return true;
		}
		return false;
	}
	public void setNewDeviceName(String newName){
		_newDeviceNameLabel = newName;
		_newDeviceName = true;
	}
	public String getNewDeviceName(){
		String result = _newDeviceNameLabel;
		_newDeviceNameLabel = null;
		return result;
	}
	
	/**
	 * Return object as xml file
	 * @return created xml string
	 */
	public String getXml(){
		XmlCreator xmlcreator = new XmlCreator(this);
		return xmlcreator.create();
	}
	
	/**
	 * Method for search all adapters by location
	 * @param name of location
	 * @return arraylist with all adapters with needed location
	 */
	public ArrayList<BaseDevice> getDevicesByLocation(String name){
		ArrayList<BaseDevice> result = new ArrayList<BaseDevice>();
		
		for(BaseDevice d : devices){
			if(d.getLocation().equals(name))
				result.add(d);
		}
		
		return result;
	}
}
