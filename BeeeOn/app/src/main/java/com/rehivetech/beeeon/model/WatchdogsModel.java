package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class WatchdogsModel extends BaseModel {

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final MultipleDataHolder<Watchdog> mWatchdogs = new MultipleDataHolder<>(); // adapterId => watchdog dataHolder

	public WatchdogsModel(INetwork network) {
		super(network);
	}

	/**
	 * Returns a watchdog by parameters
	 *
	 * @param adapterId
	 * @param id
	 * @return
	 */
	public Watchdog getWatchdog(String adapterId, String id) {
		return mWatchdogs.getObject(adapterId, id);
	}

	/**
	 * Return list of watchdogs from a gate
	 *
	 * @param adapterId
	 * @return
	 */
	public List<Watchdog> getWatchdogsByAdapter(String adapterId) {
		List<Watchdog> watchdogs = mWatchdogs.getObjects(adapterId);

		// Sort result devices by name, id
		Collections.sort(watchdogs, new NameIdentifierComparator());

		return watchdogs;
	}

	/**
	 * Reloads watchdogs
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public boolean reloadWatchdogsByAdapter(String adapterId, boolean forceReload) {
		if (!forceReload && !mWatchdogs.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mWatchdogs.setObjects(adapterId, mNetwork.getAllWatchdogs(adapterId));
		mWatchdogs.setLastUpdate(adapterId, DateTime.now());

		return true;
	}

	/**
	 * Updates watchdog in list of Watchdogs
	 *
	 * @param watchdog
	 * @return
	 */
	public boolean updateWatchdog(Watchdog watchdog) {
		if (mNetwork.updateWatchdog(watchdog, watchdog.getAdapterId())) {
			// Location was updated on server, update it in map too
			mWatchdogs.addObject(watchdog.getAdapterId(), watchdog);
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
			mWatchdogs.removeObject(watchdog.getAdapterId(), watchdog.getId());
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
		String adapterId = watchdog.getAdapterId();

		if (mNetwork.addWatchdog(watchdog, adapterId)) {
			// Watchdog was updated on server, update it in map too
			mWatchdogs.addObject(adapterId, watchdog);
			return true;
		}

		return false;
	}

}
