package cz.vutbr.fit.intelligenthomeanywhere.adapter;

import java.util.ArrayList;

import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Device;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlCreator;

/**
 * Class for parsed data from xml file of adapters
 * @author ThinkDeep
 *
 */
public class Capabilities {
	public ArrayList<Device> devices;
	private String _id;
	private String _version;
	private boolean _newInit;
	private boolean _newLocationName;
	private boolean _newDeviceName;
	private String _newDeviceNameLabel;
	
	public Capabilities() {
		devices = new ArrayList<Device>();
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
		
		for(Device dev : devices){
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
	public ArrayList<String> GetNameByLocation(String location){
		ArrayList<String> result = new ArrayList<String>();
		
		for(Device d : devices){
			if(d.GetLocation() != null && d.GetLocation().equals(location))
				result.add(d.GetName());
		}
		
		return result;
	}
	
	public void SetId(String ID){
		_id = ID;
	}
	public String GetId(){
		return _id;
	}
	
	public void SetVersion(String Version){
		_version = Version;
	}
	public String GetVersion(){
		return _version;
	}
	
	/**
	 * Method checked if is there new device in network
	 * @return true if there is new, otherwise false
	 */
	public boolean isNewOne(){
		for(Device d : devices)
			if(!d.GetInit())
				return true;
		return false;
	}
	
	/**
	 * Method return uninitialized adapter (new sensor in home)
	 * @return new adapter or null
	 */
	public Adapter GetNewOne(){
		for(Device d : devices)
			if(!d.GetInit())
				return d;
		return null;
	}
	
	/**
	 * Method search for actuator or sensor by name
	 * @param name of adapter (sensor/actuator)
	 * @return Adapter with found sensor/actuator or null
	 */
	public Adapter GetDeviceByName(String name){
		for(Device d : devices)
			if(d.GetName() != null && d.GetName().equals(name))
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
		
		for(Device d : devices){
			if(!locations.contains(d.GetLocation())){
				if(notnull)
					if(d.GetLocation() == null)
						continue;
				locations.add(d.GetLocation());
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
	public void SetNewInit(){
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
	public void SetNewLocationName(){
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
	public void SetNewDeviceName(String newName){
		_newDeviceNameLabel = newName;
		_newDeviceName = true;
	}
	public String GetNewDeviceName(){
		String result = _newDeviceNameLabel;
		_newDeviceNameLabel = null;
		return result;
	}
	
	/**
	 * Return object as xml file
	 * @return created xml string
	 */
	public String GetXml(){
		XmlCreator xmlcreator = new XmlCreator(this);
		return xmlcreator.Create();
	}
	
	/**
	 * Method for search all adapters by location
	 * @param name of location
	 * @return arraylist with all adapters with needed location
	 */
	public ArrayList<Device> GetDevicesByLocation(String name){
		ArrayList<Device> result = new ArrayList<Device>();
		
		for(Device d : devices){
			if(d.GetLocation().equals(name))
				result.add(d);
		}
		
		return result;
	}
}
