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

	private final MultipleDataHolder<Device> mUninitializedDevices = new MultipleDataHolder<>(); // gateId => mDevice dataHolder

	public UninitializedDevicesModel(INetwork network) {
		super(network);
	}

	/**
	 * Return list of all uninitialized devices from gate
	 *
	 * @param gateId
	 * @return List of uninitialized devices (or empty list)
	 */
	public List<Device> getUninitializedDevicesByGate(String gateId) {
		List<Device> devices = mUninitializedDevices.getObjects(gateId);

		// Sort result devices by id
		Collections.sort(devices, new IdentifierComparator());

		return devices;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadUninitializedDevicesByGate(String gateId, boolean forceReload) throws AppException {
		if (!forceReload && !mUninitializedDevices.isExpired(gateId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mUninitializedDevices.setObjects(gateId, mNetwork.getNewDevices(gateId));
		mUninitializedDevices.setLastUpdate(gateId, DateTime.now());

		return true;
	}

}
