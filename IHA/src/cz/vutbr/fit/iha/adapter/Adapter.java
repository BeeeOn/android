/**
 * @brief Package for adapter manipulation
 */
package cz.vutbr.fit.iha.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.format.Time;
import android.util.Log;
import cz.vutbr.fit.iha.User;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.adapter.parser.XmlCreator;

/**
 * @brief Class for parsed data from XML file of adapters
 * @author ThinkDeep
 *
 */
public class Adapter {
	public static final String TAG = Adapter.class.getSimpleName();
	
	private final Map<String, Location> mLocations = new HashMap<String, Location>();
	private final Map<String, BaseDevice> mDevices = new HashMap<String, BaseDevice>();
	private final Map<String, BaseDevice> mUninitializedDevices = new HashMap<String, BaseDevice>();
	private final Map<String, BaseDevice> mUninitializedIgnored = new HashMap<String, BaseDevice>();
	
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
		
		for(BaseDevice dev : mDevices.values()){
			result += dev.toDebugString();
			result += "__\n";
		}
		
		return result;
	}
	
	/**
	 * Set name of adapter
	 * @param name
	 */
	public void setName(String name) {
		mName = name;
	}
	
	/**
	 * Get name of adapter
	 * @return
	 */
	public String getName() {
		return mName.length() > 0 ? mName : getId();
	}
	
	/**
	 * Set role of actual user of adapter
	 * @param role
	 */
	public void setRole(User.Role role) {
		mRole = role;
	}
	
	/**
	 * Get role of actual user of adapter
	 * @return
	 */
	public User.Role getRole() {
		return mRole;
	}
	
	/**
	 * Setting id of adapter
	 * @param ID
	 */
	public void setId(String ID) {
		mId = ID;
	}
	
	/**
	 * Returning id of adapter
	 * @return id
	 */
	public String getId() {
		return mId;
	}
	
	/**
	 * Setting version of protocol
	 * @param Version
	 */
	public void setVersion(String Version) {
		mVersion = Version;
	}
	
	/**
	 * Returning version of protocol
	 * @return version
	 */
	public String getVersion() {
		return mVersion;
	}
	
	/**
	 * Find and return device by given id
	 * @param id of device
	 * @return BaseDevice or null if no device with this id is found.
	 */
	public BaseDevice getDeviceById(String id) {
		return mDevices.get(id);
	}
	
	/**
	 * Return list of all devices.
	 * @return list with devices (or empty map).
	 */
	public List<BaseDevice> getDevices() {
		return new ArrayList<BaseDevice>(mDevices.values());
	}
	
	/**
	 * Set devices that belongs to this adapter.
	 * Also updates uninitialized and locations maps.
	 * @param devices
	 */
	public void setDevices(final List<BaseDevice> devices) {
		clearDevices();

		for (BaseDevice device : devices) {
			addDevice(device);
		}
	}
	
	/**
	 * Return list of locations.
	 * @return list with locations (or empty list).
	 */
	public List<Location> getLocations() {
		return new ArrayList<Location>(mLocations.values());
	}
	
	/**
	 * Return location by id.
	 * @param id
	 * @return Location if found, null otherwise.
	 */
	public Location getLocation(String id) {
		return mLocations.get(id);
	}
	
	/**
	 * Set locations that belongs to this adapter.
	 * @param locations
	 */
	public void setLocations(final List<Location> locations) {
		mLocations.clear();
		
		for (Location location : locations) {
			mLocations.put(location.getId(), location);
		}
	}
	
	@Deprecated
	/**
	 * Return object as XML file
	 * @return created XML string
	 */
	public String getXml() {
		XmlCreator xmlcreator = new XmlCreator(this);
		return xmlcreator.create();
	}

	/**
	 * Return list of devices in specified location.
	 * @param locationId
	 * @return list with devices (or empty list)
	 */
	public List<BaseDevice> getDevicesByLocation(final String locationId) {
		List<BaseDevice> devices = new ArrayList<BaseDevice>();
		
		// Small optimization
		if (!mLocations.containsKey(locationId))
			return devices;
		
		for (BaseDevice device : mDevices.values()) {
			if (device.getLocationId().equals(locationId)) {
				devices.add(device);
			}
		}
		
		return devices;
	}
	
	/**
	 * Returns list of all uninitialized devices in this adapter
	 * @return list with uninitialized devices (or empty list)
	 */
	public List<BaseDevice> getUninitializedDevices() {
		return new ArrayList<BaseDevice>(mUninitializedDevices.values());
	}
	
	/**
	 * Add device to this listing.
	 * Also updates uninitialized and locations maps.
	 * @param device
	 * @return
	 */
	public void addDevice(final BaseDevice device) {
		mDevices.put(device.getId(), device);
		
		if (!device.isInitialized() && !mUninitializedIgnored.containsKey(device.getId()))
			mUninitializedDevices.put(device.getId(), device);
	}
	
	/**
	 * Refreshes device in listings (e.g., in uninitialized devices)
	 * @param device
	 */
	public void refreshDevice(final BaseDevice device) {
		if (device.isInitialized()) {
			if (mUninitializedDevices.remove(device.getId()) != null)
				Log.d(TAG, "Removing initialized device " + device.toString());
		} else {
			mUninitializedDevices.put(device.getId(), device);
			Log.d(TAG, "Adding uninitialized device " + device.toString());
		}
	}
	
	/**
	 * Clears all devices, uninitialized devices and locations maps.
	 */
	private void clearDevices() {
		mDevices.clear();
		mUninitializedDevices.clear();
	}

	public void ignoreUninitialized(List<BaseDevice> devices) {
		// TODO: save this list into some cache
		for (BaseDevice device : devices) {
			String id = device.getId();
			
			if (mUninitializedDevices.containsKey(id)) {
				if (!mUninitializedIgnored.containsKey(id))
					mUninitializedIgnored.put(id, mUninitializedDevices.get(id));
				
				mUninitializedDevices.remove(id);
			}
		}	
	}

	public void unignoreUninitialized() {
		// TODO: update that list in some cache
		for (BaseDevice device : mUninitializedIgnored.values()) {
			String id = device.getId();
			
			if (!mUninitializedDevices.containsKey(id))
				mUninitializedDevices.put(id, mUninitializedIgnored.get(id));
		}
		
		mUninitializedIgnored.clear();
	}
	
}
