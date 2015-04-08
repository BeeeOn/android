package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.network.INetwork;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UninitializedFacilitiesModel {

	private final INetwork mNetwork;

	private final Map<String, Map<String, Facility>> mFacilities = new HashMap<String, Map<String, Facility>>(); // adapterId => (facilityId => facility)
	private final Map<String, DateTime> mLastUpdates = new HashMap<String, DateTime>(); // adapterId => lastUpdate of facilities

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	public UninitializedFacilitiesModel(INetwork network) {
		mNetwork = network;
	}

	/**
	 * Return list of all uninitialized facilities from adapter
	 *
	 * @param adapterId
	 * @return List of uninitialized facilities (or empty list)
	 */
	public List<Facility> getUninitializedFacilitiesByAdapter(String adapterId) {
		List<Facility> facilities = new ArrayList<Facility>();

		Map<String, Facility> adapterFacilities = mFacilities.get(adapterId);
		if (adapterFacilities != null) {
			for (Facility facility : adapterFacilities.values()) {
				if (facility.getAdapterId().equals(adapterId)) {
					facilities.add(facility);
				}
			}
		}

		// Sort result facilities by id
		Collections.sort(facilities, new IdentifierComparator());

		return facilities;
	}

	private void setUninitializedFacilitiesByAdapter(String adapterId, List<Facility> facilities) {
		Map<String, Facility> adapterFacilities = mFacilities.get(adapterId);
		if (adapterFacilities != null) {
			adapterFacilities.clear();
		} else {
			adapterFacilities = new HashMap<String, Facility>();
			mFacilities.put(adapterId, adapterFacilities);
		}

		for (Facility facility : facilities) {
			adapterFacilities.put(facility.getId(), facility);
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
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadUninitializedFacilitiesByAdapter(String adapterId, boolean forceReload) throws AppException {
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

	private boolean loadFromServer(String adapterId) throws AppException {
		setUninitializedFacilitiesByAdapter(adapterId, mNetwork.getNewFacilities(adapterId));
		setLastUpdate(adapterId, DateTime.now());
		saveToCache(adapterId);

		return true;
	}

	private boolean loadFromCache(String adapterId) {
		// TODO: implement this
		return false;

		// setFacilitiesByAdapter(facilitiesFromCache);
		// setLastUpdate(adapterId, lastUpdateFromCache);
	}

	private void saveToCache(String adapterId) {
		// TODO: implement this
	}

}
