package cz.vutbr.fit.intelligenthomeanywhere.listing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class SimpleListing {

	private final Map<String, BaseDevice> mDevices = new HashMap<String, BaseDevice>();
	
	/**
	 * Return all devices.
	 * @return map with devices (or empty map).
	 */
	public Map<String, BaseDevice> getDevices() {
		return mDevices;
	}

	/**
	 * Copies all devices to this listing.
	 * @param devices
	 */
	public void setDevices(final Map<String, BaseDevice> devices) {
		mDevices.clear();
		mDevices.putAll(devices);
	}

	/**
	 * Add all devices to this listing.
	 * @param devices
	 */
	public void setDevices(final Collection<BaseDevice> devices) {
		mDevices.clear();

		for (BaseDevice device : devices) {
			mDevices.put(device.getId(), device);
		}
	}

	/**
	 * Add device to this listing.
	 * @param device
	 * @return
	 */
	public void addDevice(final BaseDevice device) {
		mDevices.put(device.getId(), device);
	}
	
	/**
	 * Remove device with specified id from this listing.
	 * @param id
	 * @return
	 */
	public void removeDevice(final String id) {
		mDevices.remove(id);
	}
	
	/**
	 * Return device with specified id.
	 * @param id
	 * @return device or null if no device with this id is found.
	 */
	public BaseDevice getById(final String id) {
		return mDevices.get(id);
	}
	
}
