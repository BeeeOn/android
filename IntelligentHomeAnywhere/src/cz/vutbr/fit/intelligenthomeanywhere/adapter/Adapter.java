/**
 * @brief Package for adapter manipulation
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlCreator;
import cz.vutbr.fit.intelligenthomeanywhere.listing.SimpleListing;

/**
 * @brief Class for parsed data from XML file of adapters
 * @author ThinkDeep
 *
 */
public class Adapter {
	/**
	 * List of devices
	 */
	public final SimpleListing devices = new SimpleListing();
	private String mId;
	private String mVersion;
	private String mName;
	private User.Role mRole;
	private boolean mNewInit = false;
	private boolean mNewLocationName = false;
	private boolean mNewDeviceName = false;
	private String mNewDeviceNameLabel;
	
	public Adapter() {}
	
	/**
	 * Debug method
	 */
	@Override
	public String toString(){
		String result = "";
		
		result += "ID is " + mId + "\n";
		result += "VERSION is " + mVersion + "\n";
		result += "Name is " + mName + "\n";
		result += "Role is " + mRole + "\n";
		result += "___start of sensors___\n";
		
		for(BaseDevice dev : devices.getDevices().values()){
			result += dev.toString(false);
			result += "__\n";
		}
		
		return result;
	}
	
	/**
	 * Set name of adapter
	 * @param name
	 */
	public void setName(String name){
		mName = name;
	}
	
	/**
	 * Get name of adapter
	 * @return
	 */
	public String getName(){
		return mName;
	}
	
	/**
	 * Set role of actual user of adapter
	 * @param role
	 */
	public void setRole(User.Role role){
		mRole = role;
	}
	
	/**
	 * Get role of actual user of adapter
	 * @return
	 */
	public User.Role getRole(){
		return mRole;
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
		for(BaseDevice d : devices.getDevices().values())
			if(!d.isInitialized())
				return true;
		return false;
	}
	
	/**
	 * Method return uninitialized adapter (new sensor in home)
	 * @return new adapter or null
	 */
	public BaseDevice getNewOne(){
		for(BaseDevice d : devices.getDevices().values())
			if(!d.isInitialized())
				return d;
		return null;
	}
	
	/**
	 * Find and return device by given id
	 * @param id of device
	 * @return BaseDeviceor null
	 */
	public BaseDevice getDeviceById(String id){
		for (BaseDevice d : devices.getDevices().values())
			if (d.getId() != null && d.getId().equals(id))
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
		
		for(BaseDevice d : devices.getDevices().values()){
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
		
		for(BaseDevice d : devices.getDevices().values()){
			if(d.getLocation().equals(name))
				result.add(d);
		}
		
		return result;
	}
}
