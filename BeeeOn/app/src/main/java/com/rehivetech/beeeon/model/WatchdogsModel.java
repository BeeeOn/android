package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class WatchdogsModel extends BaseModel {

	private final int mReloadEverySecs;

	private final MultipleDataHolder<Watchdog> mWatchdogs = new MultipleDataHolder<>(); // gateId => watchdog dataHolder

	public WatchdogsModel(INetwork network, CacheHoldTime.Item cacheHoldTime) {
		super(network);
		mReloadEverySecs = cacheHoldTime.getSeconds();
	}

	/**
	 * Returns a watchdog by parameters
	 *
	 * @param gateId
	 * @param id
	 * @return
	 */
	public Watchdog getWatchdog(String gateId, String id) {
		return mWatchdogs.getObject(gateId, id);
	}

	/**
	 * Return list of watchdogs from a gate
	 *
	 * @param gateId
	 * @return
	 */
	public List<Watchdog> getWatchdogsByGate(String gateId) {
		List<Watchdog> watchdogs = mWatchdogs.getObjects(gateId);

		// Sort result devices by name, id
		Collections.sort(watchdogs, new NameIdentifierComparator());

		return watchdogs;
	}

	/**
	 * Reloads watchdogs
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param forceReload
	 * @return
	 */
	public boolean reloadWatchdogsByGate(String gateId, boolean forceReload) {
		if (!forceReload && !mWatchdogs.isExpired(gateId, mReloadEverySecs)) {
			return false;
		}

		mWatchdogs.setObjects(gateId, mNetwork.getAllWatchdogs(gateId));
		mWatchdogs.setLastUpdate(gateId, DateTime.now());

		return true;
	}

	/**
	 * Updates watchdog in list of Watchdogs
	 *
	 * @param watchdog
	 * @return
	 */
	public boolean updateWatchdog(Watchdog watchdog) {
		if (mNetwork.updateWatchdog(watchdog, watchdog.getGateId())) {
			// Location was updated on server, update it in map too
			mWatchdogs.addObject(watchdog.getGateId(), watchdog);
			return true;
		}

		return false;
	}

	/**
	 * Delete a watchdog
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param watchdog
	 * @return
	 */
	public boolean deleteWatchdog(Watchdog watchdog) {
		// delete from server
		if (mNetwork.deleteWatchdog(watchdog)) {
			// watchdog was deleted on server, remove it from gate too
			mWatchdogs.removeObject(watchdog.getGateId(), watchdog.getId());
			return true;
		}

		return false;
	}

	/**
	 * Adds new Watchdog to list of objects
	 *
	 * @param watchdog
	 * @return
	 */
	public boolean addWatchdog(Watchdog watchdog) {
		String gateId = watchdog.getGateId();

		if (mNetwork.addWatchdog(watchdog, gateId)) {
			// Watchdog was updated on server, update it in map too
			mWatchdogs.addObject(gateId, watchdog);
			return true;
		}

		return false;
	}

}
