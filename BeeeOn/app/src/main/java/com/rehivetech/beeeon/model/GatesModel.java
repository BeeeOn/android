package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.DataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class GatesModel extends BaseModel {

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final DataHolder<Gate> mGatesHolder = new DataHolder<>();

	public GatesModel(INetwork network) {
		super(network);
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
	 * Send pair request
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @return true on successfully started pairing mode, false otherwise
	 */
	public boolean sendPairRequest(String gateId) {
		return mNetwork.prepareGateToListenNewSensors(gateId);
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
	 * FIXME: debug implementation Unregisters gate from server -> rework it when implemented in Network correctly.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param id
	 * @return true on success, false otherwise
	 */
	public boolean unregisterGate(String id, User user) {
		// FIXME: This debug implementation unregisters actual user from gate, not gate itself
		if (mNetwork.deleteAccount(id, user)) {
			reloadGates(true); // TODO: do this somehow better? Like load data only for this registered gate as answer from server?
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

}
