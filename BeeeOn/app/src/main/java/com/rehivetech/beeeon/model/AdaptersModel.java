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

public class AdaptersModel extends BaseModel {

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final DataHolder<Gate> mAdaptersHolder = new DataHolder<>();

	public AdaptersModel(INetwork network) {
		super(network);
	}

	/**
	 * Return gate with specified id or first gate, if specified one doesn't exists.
	 *
	 * @param id
	 * @return Gate if found, null otherwise.
	 */
	public Gate getAdapterOrFirst(String id) {
		return mAdaptersHolder.getObjectOrFirst(id);
	}

	/**
	 * Return all adapters that this logged in user has access to.
	 *
	 * @return List of adapters
	 */
	public List<Gate> getAdapters() {
		List<Gate> gates = mAdaptersHolder.getObjects();

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
	public Gate getAdapter(String id) {
		return mAdaptersHolder.getObject(id);
	}

	/**
	 * Send pair request
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @return true on successfully started pairing mode, false otherwise
	 */
	public boolean sendPairRequest(String adapterId) {
		return mNetwork.prepareAdapterToListenNewSensors(adapterId);
	}

	/**
	 * Registers new gate. This automatically reloads list of adapters.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param id
	 * @param name
	 * @return true on success, false otherwise
	 */
	public boolean registerAdapter(String id, String name) {
		if (mNetwork.isAvailable() && mNetwork.addAdapter(id, name)) {
			reloadAdapters(true); // TODO: do this somehow better? Like load data only for this registered gate as answer from server?
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
	public boolean unregisterAdapter(String id, User user) {
		// FIXME: This debug implementation unregisters actual user from gate, not gate itself
		if (mNetwork.deleteAccount(id, user)) {
			reloadAdapters(true); // TODO: do this somehow better? Like load data only for this registered gate as answer from server?
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
	public synchronized boolean reloadAdapters(boolean forceReload) throws AppException {
		if (!forceReload && !mAdaptersHolder.isExpired(RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mAdaptersHolder.setObjects(mNetwork.getAdapters());
		mAdaptersHolder.setLastUpdate(DateTime.now());

		return true;
	}

}
