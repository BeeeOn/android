package cz.vutbr.fit.iha.listing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;

public class SimpleListing {

	protected final Map<String, BaseDevice> mDevices = new HashMap<String, BaseDevice>();
	
	// TODO: move these 2 to Adapter? Or move this whole SimpleListing to Adapter?
	protected final Map<String, BaseDevice> mUninitializedDevices = new HashMap<String, BaseDevice>();
	protected final Map<String, BaseDevice> mUninitializedIgnored = new HashMap<String, BaseDevice>();
	
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
				Log.d("SimpleListing", "Removing initialized device " + device.toString());
		} else {
			mUninitializedDevices.put(device.getId(), device);
			Log.d("SimpleListing", "Adding uninitialized device " + device.toString());
		}
	}
	
	/**
	 * Clears all devices, uninitialized devices and locations maps.
	 */
	private void clearDevices() {
		mDevices.clear();
		mUninitializedDevices.clear();
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
	public List<BaseDevice> getByLocation(final String locationId) {
		List<BaseDevice> devices = new ArrayList<BaseDevice>();
		
		for (BaseDevice device : mDevices.values()) {
			if (device.getLocationId().equals(locationId)) {
				devices.add(device);
			}
		}
		
		return devices;
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
