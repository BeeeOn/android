package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;


public class DevicesModel extends BaseModel {

	private static final String TAG = DevicesModel.class.getSimpleName();

	private static int RELOAD_EVERY_SECONDS = 10 * 60;

	private final MultipleDataHolder<Device> mDevices = new MultipleDataHolder<>(); // gateId => mDevice dataHolder

	public DevicesModel(INetwork network,int reloadEvery) {
		super(network);
		RELOAD_EVERY_SECONDS = reloadEvery;
	}

	/**
	 * Return mDevice by ID.
	 *
	 * @param id
	 * @return mDevice or null if no mDevice is found
	 */
	public Device getDevice(String gateId, String id) {
		return mDevices.getObject(gateId, id);
	}

	/**
	 * Return module by ID
	 *
	 * @param gateId
	 * @param id
	 * @return
	 */
	public Module getModule(String gateId, String id) {
		String[] ids = id.split(Module.ID_SEPARATOR, 2);

		if (ids.length != 2) {
			throw new IllegalArgumentException(String.format("Id of module must have 2 parts, given: '%s'", id));
		}

		Device device = getDevice(gateId, ids[0]);
		if (device == null)
			return null;

		return device.getModuleById(ids[1]);
	}

	/**
	 * Return list of all devices from gate
	 *
	 * @param gateId
	 * @return List of devices (or empty list)
	 */
	public List<Device> getDevicesByGate(String gateId) {
		List<Device> devices = mDevices.getObjects(gateId);

		// Sort result devices by id
		Collections.sort(devices, new IdentifierComparator());

		return devices;
	}

	/**
	 * Return list of all devices by location from gate
	 *
	 * @param locationId
	 * @return List of devices (or empty list)
	 */
	public List<Device> getDevicesByLocation(String gateId, String locationId) {
		List<Device> devices = new ArrayList<>();

		for (Device device : getDevicesByGate(gateId)) {
			if (device.getLocationId().equals(locationId)) {
				devices.add(device);
			}
		}

		return devices;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadDevicesByGate(String gateId, boolean forceReload) throws AppException {
		if (!forceReload && !mDevices.isExpired(gateId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mDevices.setObjects(gateId, mNetwork.initGate(gateId));
		mDevices.setLastUpdate(gateId, DateTime.now());

		return true;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param devices
	 * @return
	 */
	public boolean refreshDevices(List<Device> devices, boolean forceReload) throws AppException {
		// Remove not expired devices

		// NOTE: changed from foreach version to this -> http://stackoverflow.com/questions/1196586/calling-remove-in-foreach-loop-in-java
		Iterator<Device> facIterator = devices.iterator();
		while (facIterator.hasNext()) {
			Device device = facIterator.next();
			if (!forceReload && !device.isExpired()) {
				facIterator.remove();
			}
		}

		List<Device> newDevices = mNetwork.getDevices(devices);
		if (newDevices == null)
			return false;

		for (Device newDevice : newDevices) {
			mDevices.addObject(newDevice.getGateId(), newDevice);
		}

		return true;
	}

	/**
	 * This reloads data of device from server...
	 * This CAN'T be called on UI thread!
	 *
	 * @param device
	 * @return
	 */
	public boolean refreshDevice(Device device, boolean forceReload) throws AppException {
		if (!forceReload && !device.isExpired()) {
			return false;
		}

		Device newDevice = mNetwork.getDevice(device);
		if (newDevice == null)
			return false;

		mDevices.addObject(device.getGateId(), device);

		return true;
	}

	/**
	 * Save specified settings of device to server.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param device
	 * @param what   type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveDevice(Device device, EnumSet<SaveModule> what) throws AppException {
		mNetwork.updateDevice(device.getGateId(), device, what);
		refreshDevice(device, true);

		return true;
	}

	/**
	 * Delete device from server.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 */
	public boolean deleteDevice(Device device) throws AppException {
		if (mNetwork.deleteDevice(device)) {
			// Device was deleted on server, remove it from map too
			mDevices.removeObject(device.getGateId(), device.getId());
			return true;
		}

		return false;
	}

	/**
	 * Send request to server to switch Actor value.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param module ModuleType of this module must be actor, i.e., module.getType().isActor() must return true.
	 * @return true on success, false otherwise
	 */
	public boolean switchActor(Module module) throws AppException {
		if (!module.isActuator()) {
			Log.e(TAG, String.format("Tried to switch NOT-actor module '%s'", module.getAbsoluteId()));
			return false;
		}

		Device device = module.getDevice();

		mNetwork.switchState(module.getDevice().getGateId(), module);
		refreshDevice(device, true);

		return true;
	}

}
