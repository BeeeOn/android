package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class WatchDogsModel extends BaseModel {

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final MultipleDataHolder<WatchDog> mWatchDogs = new MultipleDataHolder<>(); // adapterId => watchDog dataHolder

	public WatchDogsModel(INetwork network) {
		super(network);
	}

	/**
	 * Returns a watchdog by parameters
	 * @param adapterId
	 * @param id
	 * @return
	 */
	public WatchDog getWatchDog(String adapterId, String id) {
		return mWatchDogs.getObject(adapterId, id);
	}

	/**
	 * Return list of watchdogs from a adapter
	 * @param adapterId
	 * @return
	 */
	public List<WatchDog> getWatchDogsByAdapter(String adapterId) {
		List<WatchDog> watchdogs = mWatchDogs.getObjects(adapterId);

		// Sort result facilities by name, id
		Collections.sort(watchdogs, new NameIdentifierComparator());

		return watchdogs;
	}

	/**
	 * Reloads watchdogs
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public boolean reloadWatchDogsByAdapter(String adapterId, boolean forceReload) {
		if (!forceReload && !mWatchDogs.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mWatchDogs.setObjects(adapterId, mNetwork.getAllWatchDogs(adapterId));
		mWatchDogs.setLastUpdate(adapterId, DateTime.now());

		return true;
	}

	/**
	 * Updates watchdog in list of WatchDogs
	 *
	 * @param watchdog
	 * @return
	 */
	public boolean updateWatchDog(WatchDog watchdog) {
		if (mNetwork.updateWatchDog(watchdog, watchdog.getAdapterId())) {
			// Location was updated on server, update it in map too
			mWatchDogs.addObject(watchdog.getAdapterId(), watchdog);
			return true;
		}

		return false;
	}

	/**
	 * Delete a watchdog
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param watchdog
	 * @return
	 */
	public boolean deleteWatchDog(WatchDog watchdog) {
		// delete from server
		if (mNetwork.deleteWatchDog(watchdog)) {
			// watchdog was deleted on server, remove it from adapter too
			mWatchDogs.removeObject(watchdog.getAdapterId(), watchdog.getId());
			return true;
		}

		return false;
	}

	/**
	 * Adds new WatchDog to list of objects
	 *
	 * @param watchdog
	 * @return
	 */
	public boolean addWatchDog(WatchDog watchdog) {
		String adapterId = watchdog.getAdapterId();

		if (mNetwork.addWatchDog(watchdog, adapterId)) {
			// WatchDog was updated on server, update it in map too
			mWatchDogs.addObject(adapterId, watchdog);
			return true;
		}

		return false;
	}

}
