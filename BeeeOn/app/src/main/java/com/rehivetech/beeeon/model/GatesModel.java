package com.rehivetech.beeeon.model;

import android.content.SharedPreferences;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.DataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class GatesModel extends BaseModel {

	private static  int RELOAD_EVERY_SECONDS = 10 * 60;

	private final DataHolder<Gate> mGatesHolder = new DataHolder<>();

	private final DataHolder<GateInfo> mGatesInfoHolder = new DataHolder<>();

	public GatesModel(INetwork network,SharedPreferences prefs) {
		super(network);
		RELOAD_EVERY_SECONDS = Integer.parseInt(prefs.getString(CacheHoldTime.PERSISTENCE_CACHE_KEY, "0"));
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
	 * @return true on successfully started pairing mode, false otherwise
	 */
	public boolean sendPairRequest(String gateId) {
		return mNetwork.prepareGateToListenNewDevices(gateId);
	}

	/**
	 * Registers new gate. This automatically reloads list of gates.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param id
	 * @param name
	 * @return true on success, false otherwise
	 */
	public boolean registerGate(String id, String name) {
		if (mNetwork.isAvailable() && mNetwork.addGate(id, name)) {
			reloadGates(true); // TODO: do this somehow better? Like load data only for this registered gate as answer from server?
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
		if (mNetwork.deleteGate(gateId)) {
			reloadGates(true); // TODO: do this somehow better? Like load data only for this registered adapter as answer from server?
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
		if (!forceReload && !mGatesHolder.isExpired(RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mGatesHolder.setObjects(mNetwork.getGates());
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
		/*if (!forceReload && !mGatesInfoHolder.isExpired(RELOAD_EVERY_SECONDS)) {
			return false;
		}*/

		GateInfo gateInfo = mNetwork.getGateInfo(gateId);
		if (gateInfo == null)
			return false;

		mGatesInfoHolder.addObject(gateInfo);
		return true;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param gate - new edited gate
	 * @return
	 */
	public boolean editGate(Gate gate) {
		if (mNetwork.updateGate(gate)) {
			// Invalidate gates and gatesInfo caches
			// FIXME: don't do it by deleting whole object, but just setting isExpired for single item
			mGatesInfoHolder.removeObject(gate.getId());
			mGatesHolder.setLastUpdate(null);
			return true;
		}

		return false;
	}

}
