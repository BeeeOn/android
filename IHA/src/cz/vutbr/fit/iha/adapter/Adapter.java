/**
 * @brief Package for adapter manipulation
 */
package cz.vutbr.fit.iha.adapter;

import java.util.ArrayList;
import java.util.List;

import android.text.format.Time;
import cz.vutbr.fit.iha.User;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.parser.XmlCreator;
import cz.vutbr.fit.iha.listing.Location;
import cz.vutbr.fit.iha.listing.SimpleListing;

/**
 * @brief Class for parsed data from XML file of adapters
 * @author ThinkDeep
 *
 */
public class Adapter {
	/**
	 * List of devices
	 */
	private final SimpleListing mDevices = new SimpleListing();
	private String mId = "";
	private String mVersion = "";
	private String mName = "";
	private User.Role mRole;
	
	public final Time lastUpdate = new Time();
	
	public Adapter() {}
	
	/**
	 * Debug method
	 */
	public String toDebugString() {
		String result = "";

		result += "ID is " + mId + "\n";
		result += "VERSION is " + mVersion + "\n";
		result += "Name is " + mName + "\n";
		result += "Role is " + mRole + "\n";
		result += "___start of sensors___\n";
		
		for(BaseDevice dev : mDevices.getDevices()){
			result += dev.toDebugString();
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
		return mName.length() > 0 ? mName : getId();
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
	 * Find and return device by given id
	 * @param id of device
	 * @return BaseDevice or null
	 */
	public BaseDevice getDeviceById(String id){
		return mDevices.getById(id);
	}
	
	/**
	 * Return map with all devices;
	 * @return map with all devices (or empty map)
	 */
	public List<BaseDevice> getDevices() {
		return mDevices.getDevices();
	}
	
	/**
	 * Set devices that belongs to this adapter.
	 * @param devices
	 */
	public void setDevices(List<BaseDevice> devices) {
		mDevices.setDevices(devices);
	}
	
	/**
	 * Return list of locations.
	 * @return list with locations (or empty list).
	 */
	public List<Location> getLocations(){
		return mDevices.getLocations();
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
	public List<BaseDevice> getDevicesByLocation(String location){
		return mDevices.getByLocation(location);
	}
	
	/**
	 * Returns list of all uninitialized devices in this adapter
	 * @return
	 */
	public List<BaseDevice> getUninitializedDevices() {
		return new ArrayList<BaseDevice>(mDevices.getUninitializedDevices().values());
	}
	
	/**
	 * Refreshes device in listings (e.g., in uninitialized devices)
	 * @param device
	 */
	public void refreshDevice(final BaseDevice device) {
		mDevices.refreshDevice(device);
	}
	
}
