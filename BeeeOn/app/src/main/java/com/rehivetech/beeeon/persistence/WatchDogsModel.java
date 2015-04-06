package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.household.watchdog.WatchDog;

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

	public WatchDog getWatchDog(String adapterId, String id) {
		Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(adapterId);
		if (adapterWatchDogs == null) {
			return null;
		}

		return adapterWatchDogs.get(id);
	}

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

	public boolean reloadWatchDogsByAdapter(String adapterId, boolean forceReload) {
		if (!forceReload && !isExpired(adapterId)) {
			return false;
		}

		// TODO: check if user is logged in
		if (mNetwork.isAvailable()) {
			return loadFromServer(adapterId);
		} else if (forceReload) {
			return loadFromCache(adapterId);
		}

		return false;
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

	/**
	 * Updates watchdog in list of WatchDogs
	 * @param adapterId
	 * @param watchdog
	 * @return
	 */
	public boolean updateWatchDog(String adapterId, WatchDog watchdog) {
		Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(adapterId);

		// TODO: check/create adapterLocations object
		if (!adapterWatchDogs.containsKey(watchdog.getId())) {
			// //Log.w(TAG, String.format("Can't update location with id=%s. It doesn't exists.", location.getId()));
			return false;
		}

		adapterWatchDogs.put(watchdog.getId(), watchdog);
		return true;

	}

	/**
	 * Removes location from list of locations.
	 *
	 * @param id
	 * @return false when there wasn't location with this id
	 */
	public boolean deleteWatchDog(String adapterId, String id) {
		// TODO: review and refactor, this is just copied from Adapter
		// TODO: check/create adapterLocations object
		Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(adapterId);
		return adapterWatchDogs.remove(id) != null;
	}

	/**
	 * Adds new WatchDog to list of objects
	 * @param adapterId
	 * @param watchdog
	 * @return
	 */
	public boolean addWatchDog(String adapterId, WatchDog watchdog) {
		// TODO check if it's ok this way
		Map<String, WatchDog> adapterWatchDogs = mWatchDogs.get(adapterId);
		if(adapterWatchDogs == null){
			mWatchDogs.put(adapterId, new HashMap<String, WatchDog>());
			adapterWatchDogs = mWatchDogs.get(adapterId);
		}

		if (adapterWatchDogs.containsKey(watchdog.getId()))
			return false;

		adapterWatchDogs.put(watchdog.getId(), watchdog);
		return true;
	}
}
