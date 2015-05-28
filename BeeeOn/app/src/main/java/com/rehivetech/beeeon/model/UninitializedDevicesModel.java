package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class UninitializedDevicesModel extends BaseModel {

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final MultipleDataHolder<Device> mUninitializedDevices = new MultipleDataHolder<>(); // adapterId => mDevice dataHolder

	public UninitializedDevicesModel(INetwork network) {
		super(network);
	}

	/**
	 * Return list of all uninitialized devices from gate
	 *
	 * @param adapterId
	 * @return List of uninitialized devices (or empty list)
	 */
	public List<Device> getUninitializedDevicesByAdapter(String adapterId) {
		List<Device> devices = mUninitializedDevices.getObjects(adapterId);

		// Sort result devices by id
		Collections.sort(devices, new IdentifierComparator());

		return devices;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadUninitializedDevicesByAdapter(String adapterId, boolean forceReload) throws AppException {
		if (!forceReload && !mUninitializedDevices.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mUninitializedDevices.setObjects(adapterId, mNetwork.getNewDevices(adapterId));
		mUninitializedDevices.setLastUpdate(adapterId, DateTime.now());

		return true;
	}

}
