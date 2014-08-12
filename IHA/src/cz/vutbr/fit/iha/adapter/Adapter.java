/**
 * @brief Package for adapter manipulation
 */
package cz.vutbr.fit.iha.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.text.format.Time;
import android.util.Log;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.household.User;

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
	private String mName = "";
	private User.Role mRole;
	
	public final Time lastUpdate = new Time();
	
	public Adapter() {}
	
	/**
	 * Debug method
	 */
	public String toDebugString() {
		String devices = "";
		for (BaseDevice dev : mDevices.values()) {
			devices += String.format(" - %s\n", dev.toDebugString());
		}
		
		return String.format("Id: %s\nName: %s\nRole: %s\nDevices:\n%s", mId, mName, mRole, devices);
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
			addLocation(location);
		}
	}
	
	/**
	 * Adds new location to list of locations. 
	 * @param location
	 * @return false if there is already location with this id
	 */
	public boolean addLocation(Location location) {
		if (mLocations.containsKey(location.getId()))
			return false;

		mLocations.put(location.getId(), location);
		return true;
	}
	
	/**
	 * Removes location from list of locations.
	 * @param id
	 * @return false when there wasn't location with this id
	 */
	public boolean deleteLocation(String id) {
		return mLocations.remove(id) != null;
	}
	
	/**
	 * Updates location in list of locations.
	 * @param location
	 * @return
	 */
	public boolean updateLocation(Location location) {
		if (!mLocations.containsKey(location.getId())) {
			Log.w(TAG, String.format("Can't update location with id=%s. It doesn't exists.", location.getId()));
			return false;
		}
		
		mLocations.put(location.getId(), location);
		return true;
	}

	/**
	 * Return list of devices in specified location.
	 * @param id
	 * @return list with devices (or empty list)
	 */
	public List<BaseDevice> getDevicesByLocation(final String id) {
		List<BaseDevice> devices = new ArrayList<BaseDevice>();
		
		// Small optimization
		if (!mLocations.containsKey(id))
			return devices;
		
		for (BaseDevice device : mDevices.values()) {
			if (device.getLocationId().equals(id)) {
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
		
		if (!mLocations.containsKey(device.getLocationId()))
			Log.w(TAG, "Adding device with unknown locationId: " + device.getLocationId());
		
		if (!device.isInitialized() && !mUninitializedIgnored.containsKey(device.getId()))
			mUninitializedDevices.put(device.getId(), device);
	}
	
	/**
	 * Refreshes device in listings (e.g., in uninitialized devices)
	 * @param device
	 * @return false if adapter doesn't contain this device, true otherwise
	 */
	public boolean refreshDevice(final BaseDevice device) {
		if (!mDevices.containsKey(device.getId())) {
			Log.w(TAG, String.format("Can't refresh device '%s', adapter '%s' doesn't contain it", device.getName(), getName()));
			return false;
		}
		
		if (device.isInitialized()) {
			if (mUninitializedDevices.remove(device.getId()) != null)
				Log.d(TAG, "Removing initialized device " + device.toString());
		} else {
			mUninitializedDevices.put(device.getId(), device);
			Log.d(TAG, "Adding uninitialized device " + device.toString());
		}
		return true;
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

	/**
	 * This is used ONLY for DemoMode when saving new location! 
	 * @return unique id of location
	 */
	public String getUnusedLocationId() {		
		String id;
		Random random = new Random();

		do {
			id = String.valueOf(random.nextInt(1000));
		} while (mLocations.containsKey(id));
		
		return id;
	}
	
}
