package cz.vutbr.fit.intelligenthomeanywhere.listing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class SimpleListing {

	protected final Map<String, BaseDevice> mDevices = new HashMap<String, BaseDevice>();
	protected final Map<String, BaseDevice> mUninitializedDevices = new HashMap<String, BaseDevice>();
	protected final Map<String, Location> mLocations = new HashMap<String, Location>();
	
	/**
	 * Return list of all devices.
	 * @return list with devices (or empty map).
	 */
	public List<BaseDevice> getDevices() {
		return new ArrayList<BaseDevice>(mDevices.values());
	}
	
	/**
	 * Return all uninitialized devices.
	 * @return map with devices (or empty map).
	 */
	public Map<String, BaseDevice> getUninitializedDevices() {
		return mUninitializedDevices;
	}
	
	/**
	 * Return list of locations.
	 * @return list with locations (or empty list).
	 */
	public List<Location> getLocations() {
		return new ArrayList<Location>(mLocations.values());
	}

	/**
	 * Add all devices to this listing.
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
	 * Add device to this listing.
	 * Also updates uninitialized and locations maps.
	 * @param device
	 * @return
	 */
	public void addDevice(final BaseDevice device) {
		mDevices.put(device.getId(), device);
		
		if (!device.isInitialized())
			mUninitializedDevices.put(device.getId(), device);
		
		Location location = device.getLocation();
		if (location.getId().length() > 0 && !mLocations.containsValue(location))
			mLocations.put(location.getId(), location);
	}
	
	/**
	 * Clears all devices, uninitialized devices and locations maps.
	 */
	private void clearDevices() {
		mDevices.clear();
		mUninitializedDevices.clear();
		mLocations.clear();
	}
	
	/**
	 * Return device with specified id.
	 * @param id
	 * @return device or null if no device with this id is found.
	 */
	public BaseDevice getById(final String id) {
		return mDevices.get(id);
	}
	
	/**
	 * Return list of devices in specified location.
	 * @param location
	 * @return list with devices (or empty list)
	 */
	public List<BaseDevice> getByLocation(final String location) {
		List<BaseDevice> devices = new ArrayList<BaseDevice>();
		
		if (mLocations.containsKey(location)) {
			for (BaseDevice device : mDevices.values()) {
				if (device.getLocation().getName().equals(location)) {
					devices.add(device);
				}
			}
		}
		
		return devices;
	}
	
}
