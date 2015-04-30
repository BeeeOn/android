package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class UninitializedFacilitiesModel {

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final INetwork mNetwork;

	private final MultipleDataHolder<Facility> mUninitializedFacilities = new MultipleDataHolder<>(); // adapterId => facility dataHolder

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
		List<Facility> facilities = mUninitializedFacilities.getObjects(adapterId);

		// Sort result facilities by id
		Collections.sort(facilities, new IdentifierComparator());

		return facilities;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadUninitializedFacilitiesByAdapter(String adapterId, boolean forceReload) throws AppException {
		if (!forceReload && !mUninitializedFacilities.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mUninitializedFacilities.setObjects(adapterId, mNetwork.getNewFacilities(adapterId));
		mUninitializedFacilities.setLastUpdate(adapterId, DateTime.now());

		return true;
	}

}
