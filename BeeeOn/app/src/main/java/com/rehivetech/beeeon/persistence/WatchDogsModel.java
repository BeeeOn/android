package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.network.INetwork;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchDogsModel {

	private static final String TAG = WatchDogsModel.class.getSimpleName();

	private final INetwork mNetwork;

	private final Map<String, Map<String, WatchDog>> mWatchDogs = new HashMap<String, Map<String, WatchDog>>(); // adapterId => (watchdogId => watchdog)
	private final Map<String, DateTime> mLastUpdates = new HashMap<String, DateTime>(); // adapterId => lastUpdate of facilities

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	public WatchDogsModel(INetwork network) {
		mNetwork = network;
	}

	/**
	 * Returns a watchdog by parameters
	 * @param adapterId
	 * @param id
	 * @return
	 */
	public WatchDog getWatchDog(String adapterId, String id) {
		Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(adapterId);
		if (adapterWatchDogs == null) {
			return null;
		}

		return adapterWatchDogs.get(id);
	}

	/**
	 * Return list of watchdogs from a adapter
	 * @param adapterId
	 * @return
	 */
	public List<WatchDog> getWatchDogsByAdapter(String adapterId) {
		List<WatchDog> watchdogs = new ArrayList<WatchDog>();

		Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(adapterId);
		if (adapterWatchDogs != null) {
			for (WatchDog watchdog : adapterWatchDogs.values()) {
				if (watchdog.getAdapterId().equals(adapterId)) {
					watchdogs.add(watchdog);
				}
			}
		}

		// Sort result facilities by id
		Collections.sort(watchdogs, new IdentifierComparator());

		return watchdogs;
	}

	private void setWatchDogsByAdapter(String adapterId, List<WatchDog> watchdogs) {
		Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(adapterId);
		if (adapterWatchDogs != null) {
			adapterWatchDogs.clear();
		} else {
			adapterWatchDogs = new HashMap<String, WatchDog>();
			mWatchDogs.put(adapterId, adapterWatchDogs);
		}

		for (WatchDog watchdog : watchdogs) {
			adapterWatchDogs.put(watchdog.getId(), watchdog);
		}
	}

	private void setLastUpdate(String adapterId, DateTime lastUpdate) {
		mLastUpdates.put(adapterId, lastUpdate);
	}

	private boolean isExpired(String adapterId) {
		DateTime lastUpdate = mLastUpdates.get(adapterId);
		return lastUpdate == null || lastUpdate.plusSeconds(RELOAD_EVERY_SECONDS).isBeforeNow();
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
		if (!forceReload && !isExpired(adapterId)) {
			return false;
		}

		// Don't check availability as we don't have cache working, so let Network notify connection error eventually
		return loadFromServer(adapterId);

		/*if (mNetwork.isAvailable()) {
			return loadFromServer(adapterId);
		} else if (forceReload) {
			return loadFromCache(adapterId);
		}

		return false;*/
	}

	private boolean loadFromServer(String adapterId) {
		try {
			setWatchDogsByAdapter(adapterId, mNetwork.getAllWatchDogs(adapterId));
			setLastUpdate(adapterId, DateTime.now());
			saveToCache(adapterId);
		} catch (AppException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private boolean loadFromCache(String adapterId) {
		// TODO: implement this
		return false;

		// setWatchDogsByAdapter(watchdogsFromCache);
		// setLastUpdate(adapterId, lastUpdateFromCache);
	}

	private void saveToCache(String adapterId) {
		// TODO: implement this
	}

	private void updateWatchDogInMap(WatchDog watchdog) {
		String adapterId = watchdog.getAdapterId();

		Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(adapterId);
		if (adapterWatchDogs == null) {
			adapterWatchDogs = new HashMap<String, WatchDog>();
			mWatchDogs.put(adapterId, adapterWatchDogs);
		}

		adapterWatchDogs.put(watchdog.getId(), watchdog);
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
			updateWatchDogInMap(watchdog);
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
			Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(watchdog.getAdapterId());
			if (adapterWatchDogs != null)
				adapterWatchDogs.remove(watchdog.getId());

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
		if (mNetwork.addWatchDog(watchdog, watchdog.getAdapterId())) {
			// WatchDog was updated on server, update it in map too
			updateWatchDogInMap(watchdog);
			return true;
		}

		return false;
	}

}
