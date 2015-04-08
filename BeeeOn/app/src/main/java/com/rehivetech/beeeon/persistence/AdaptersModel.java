package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptersModel {

	private final INetwork mNetwork;

	private final Map<String, Adapter> mAdapters = new HashMap<String, Adapter>();

	private DateTime mLastUpdate;

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	public AdaptersModel(INetwork network) {
		mNetwork = network;
	}

	public Map<String, Adapter> getAdaptersMap() {
		return mAdapters;
	}

	/**
	 * Return all adapters that this logged in user has access to.
	 *
	 * @return List of adapters
	 */
	public List<Adapter> getAdapters() {
		List<Adapter> adapters = new ArrayList<Adapter>();

		for (Adapter adapter : mAdapters.values()) {
			adapters.add(adapter);
		}

		// Sort result adapters by id
		Collections.sort(adapters, new NameIdentifierComparator());

		return adapters;
	}

	private void setAdapters(List<Adapter> adapters) {
		mAdapters.clear();

		for (Adapter adapter : adapters) {
			mAdapters.put(adapter.getId(), adapter);
		}
	}

	/**
	 * Return adapter by his ID.
	 *
	 * @param id
	 * @return Adapter if found, null otherwise
	 */
	public Adapter getAdapter(String id) {
		return mAdapters.get(id);
	}

	private void setLastUpdate(DateTime lastUpdate) {
		mLastUpdate = lastUpdate;
	}

	private boolean isExpired() {
		return mLastUpdate == null || mLastUpdate.plusSeconds(RELOAD_EVERY_SECONDS).isBeforeNow();
	}

	/**
	 * Send pair request
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @return true on successfully started pairing mode, false otherwise
	 */
	public boolean sendPairRequest(String adapterId) {
		return mNetwork.prepareAdapterToListenNewSensors(adapterId);
	}

	/**
	 * Registers new adapter. This automatically reloads list of adapters.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param id
	 * @param name
	 * @return true on success, false otherwise
	 */
	public boolean registerAdapter(String id, String name) {
		if (mNetwork.isAvailable() && mNetwork.addAdapter(id, name)) {
			reloadAdapters(true); // TODO: do this somehow better? Like load data only for this registered adapter as answer from server?
			return true;
		}

		return false;
	}

	/**
	 * FIXME: debug implementation Unregisters adapter from server -> rework it when implemented in Network correctly.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param id
	 * @return true on success, false otherwise
	 */
	public boolean unregisterAdapter(String id, User user) {
		// FIXME: This debug implementation unregisters actual user from adapter, not adapter itself
		if (mNetwork.deleteAccount(id, user)) {
			reloadAdapters(true); // TODO: do this somehow better? Like load data only for this registered adapter as answer from server?
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
		if (!forceReload && !isExpired()) {
			return false;
		}

		setAdapters(mNetwork.getAdapters());
		setLastUpdate(DateTime.now());

		return true;
	}

}
