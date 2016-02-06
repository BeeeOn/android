package com.rehivetech.beeeon.model;

import android.support.annotation.Nullable;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.DataHolder;
import com.rehivetech.beeeon.util.GpsData;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class GatesModel extends BaseModel {

	private final int mReloadEverySecs;

	private final DataHolder<Gate> mGatesHolder = new DataHolder<>();

	private final DataHolder<GateInfo> mGatesInfoHolder = new DataHolder<>();

	public GatesModel(INetwork network, CacheHoldTime.Item cacheHoldTime) {
		super(network);
		mReloadEverySecs = cacheHoldTime.getSeconds();
	}

	/**
	 * Return gate with specified id or first gate, if specified one doesn't exists.
	 *
	 * @param id
	 * @return Gate if found, null otherwise.
	 */
	public Gate getGateOrFirst(String id) {
		return mGatesHolder.getObjectOrFirst(id);
	}

	/**
	 * Return all gates that this logged in user has access to.
	 *
	 * @return List of gates
	 */
	public List<Gate> getGates() {
		List<Gate> gates = mGatesHolder.getObjects();

		// Sort result gates by name, id
		Collections.sort(gates, new NameIdentifierComparator());

		return gates;
	}

	/**
	 * Return gate by his ID.
	 *
	 * @param id
	 * @return Gate if found, null otherwise
	 */
	public Gate getGate(String id) {
		return mGatesHolder.getObject(id);
	}

	/**
	 * Return gate info by its ID.
	 *
	 * @param id
	 * @return GateInfo if found, null otherwise
	 */
	public GateInfo getGateInfo(String id) {
		return mGatesInfoHolder.getObject(id);
	}

	/**
	 * Send pair request
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param deviceIpAddress if specified, sending search instead of startListen
	 * @return true on successfully started pairing mode, false otherwise
	 */
	public boolean sendPairRequest(String gateId, @Nullable String deviceIpAddress) {
		return deviceIpAddress != null ? mNetwork.gates_search(gateId, deviceIpAddress) : mNetwork.gates_startListen(gateId);
	}

	/**
	 * Registers new gate. This automatically reloads list of gates.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param id
	 * @param name
	 * @param offsetInMinutes
	 * @return true on success, false otherwise
	 */
	public boolean registerGate(String id, String name, int offsetInMinutes) {
		if (mNetwork.isAvailable() && mNetwork.gates_register(id, name, offsetInMinutes)) {
			reloadGates(true); // TODO: do this somehow better? Like load data only for this registered gate as answer from server?
			reloadGateInfo(id, true);
			return true;
		}

		return false;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @return true on success, false otherwise
	 */
	public boolean unregisterGate(String gateId) {
		if (mNetwork.gates_unregister(gateId)) {
			// Gate was deleted on server, remove it from map too
			mGatesHolder.removeObject(gateId);
			mGatesInfoHolder.removeObject(gateId);
			return true;
		}

		return false;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadGates(boolean forceReload) throws AppException {
		if (!forceReload && !mGatesHolder.isExpired(mReloadEverySecs)) {
			return false;
		}

		mGatesInfoHolder.clear();

		mGatesHolder.setObjects(mNetwork.gates_getAll());
		mGatesHolder.setLastUpdate(DateTime.now());

		return true;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadGateInfo(String gateId, boolean forceReload) throws AppException {
		// FIXME: Check isExpired for single item
		/*if (!forceReload && !mGatesInfoHolder.isExpired(mReloadEverySecs)) {
			return false;
		}*/

		GateInfo gateInfo = mNetwork.gates_get(gateId);
		if (gateInfo == null)
			return false;

		mGatesInfoHolder.addObject(gateInfo);
		return true;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param gate - new edited gate
	 * @param gpsData
	 * @return
	 */
	public boolean editGate(Gate gate, GpsData gpsData) {
		if (mNetwork.gates_update(gate, gpsData)) {
			// Invalidate gates and gatesInfo caches
			// FIXME: don't do it by deleting whole object, but just setting isExpired for single item
			mGatesInfoHolder.removeObject(gate.getId());
			mGatesHolder.setLastUpdate(null);
			return true;
		}

		return false;
	}

}
