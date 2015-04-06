package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Device.SaveDevice;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacilitiesModel {

	private static final String TAG = FacilitiesModel.class.getSimpleName();
	
	private final INetwork mNetwork;

	private final Map<String, Map<String, Facility>> mFacilities = new HashMap<String, Map<String, Facility>>(); // adapterId => (facilityId => facility)
	private final Map<String, DateTime> mLastUpdates = new HashMap<String, DateTime>(); // adapterId => lastUpdate of facilities

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	public FacilitiesModel(INetwork network) {
		mNetwork = network;
	}

	/**
	 * Return facility by ID.
	 *
	 * @param id
	 * @return facility or null if no facility is found
	 */
	public Facility getFacility(String adapterId, String id) {
		Map<String, Facility> adapterFacilities = mFacilities.get(adapterId);
		if (adapterFacilities == null) {
			return null;
		}

		return adapterFacilities.get(id);
	}

	/**
	 * Return device by ID
	 *
	 * @param adapterId
	 * @param id
	 * @return
	 */
	public Device getDevice(String adapterId, String id) {
		String[] ids = id.split(Device.ID_SEPARATOR, 2);

		Facility facility = getFacility(adapterId, ids[0]);
		if (facility == null)
			return null;

		// FIXME: cleanup this after demo

		int iType = -1; // unknown type
		int offset = 0; // default offset

		if (!ids[1].isEmpty()) {
			// Get integer representation of the given string value
			int value = Integer.parseInt(ids[1]);

			// Separate combined value to type and offset
			iType = value % 256;
			offset = value / 256;
		}

		DeviceType type = DeviceType.fromTypeId(iType);

		return facility.getDeviceByType(type, offset);
	}

	/**
	 * Return list of all facilities from adapter
	 *
	 * @param adapterId
	 * @return List of facilities (or empty list)
	 */
	public List<Facility> getFacilitiesByAdapter(String adapterId) {
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

	private void setFacilitiesByAdapter(String adapterId, List<Facility> facilities) {
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

	/**
	 * Return list of all facilities by location from adapter
	 *
	 * @param locationId
	 * @return List of facilities (or empty list)
	 */
	public List<Facility> getFacilitiesByLocation(String adapterId, String locationId) {
		List<Facility> facilities = new ArrayList<Facility>();

		for (Facility facility : getFacilitiesByAdapter(adapterId)) {
			if (facility.getLocationId().equals(locationId)) {
				facilities.add(facility);
			}
		}

		return facilities;
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
	public synchronized boolean reloadFacilitiesByAdapter(String adapterId, boolean forceReload) throws AppException {
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

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param facilities
	 * @return
	 */
	public boolean refreshFacilities(List<Facility> facilities, boolean forceReload) throws AppException {
		// Remove not expired facilities
		for (Facility facility : facilities) {
			if (!forceReload && !facility.isExpired()) {
				facilities.remove(facility);
			}
		}

		List<Facility> newFacilities = mNetwork.getFacilities(facilities);
		if (newFacilities == null)
			return false;

		for (Facility newFacility : newFacilities) {
			updateFacilityInMap(newFacility);
		}

		return true;
	}

	private boolean loadFromServer(String adapterId) throws AppException {
		setFacilitiesByAdapter(adapterId, mNetwork.initAdapter(adapterId));
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

	private void updateFacilityInMap(Facility facility) {
		String adapterId = facility.getAdapterId();

		Map<String, Facility> adapterFacilities = mFacilities.get(adapterId);
		if (adapterFacilities == null) {
			adapterFacilities = new HashMap<String, Facility>();
			mFacilities.put(adapterId, adapterFacilities);
		}

		adapterFacilities.put(facility.getId(), facility);
	}

	/**
	 * This reloads data of facility from server...
	 * This CAN'T be called on UI thread!
	 *
	 * @param facility
	 * @return
	 */
	public boolean refreshFacility(Facility facility, boolean forceReload) throws AppException {
		if (!forceReload && !facility.isExpired()) {
			return false;
		}
		
		Facility newFacility = mNetwork.getFacility(facility);
		if (newFacility == null)
			return false;

		updateFacilityInMap(facility);

		return true;
	}

	/**
	 * Save specified settings of facility to server.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param facility
	 * @param what
	 *            type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveFacility(Facility facility, EnumSet<SaveDevice> what) throws AppException {
		mNetwork.updateFacility(facility.getAdapterId(), facility, what);
		refreshFacility(facility, true);

		return true;
	}

	/**
	 * Delete facility
	 *
	 * This CAN'T be called on UI thread!
	 */
	public boolean deleteFacility(Facility facility) throws AppException {
        String adapterId = facility.getAdapterId();
		mNetwork.deleteFacility(adapterId,facility);
        refreshFacilities(getFacilitiesByAdapter(adapterId), true);

        return true;
    }

	/**
	 * Save specified settings of device to server.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param device
	 * @param what
	 *            type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveDevice(Device device, EnumSet<SaveDevice> what) throws AppException {
		Facility facility = device.getFacility();

		mNetwork.updateDevice(facility.getAdapterId(), device, what);
		refreshFacility(facility, true);

		return true;
	}

	/**
	 * Send request to server to switch Actor value.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param device
	 *            DeviceType of this device must be actor, i.e., device.getType().isActor() must return true.
	 * @return true on success, false otherwise
	 */
	public boolean switchActor(Device device) throws AppException {
		if (!device.getType().isActor()) {
			Log.e(TAG, String.format("Tried to switch NOT-actor device '%s'", device.getName()));
			return false;
		}
		
		Facility facility = device.getFacility();

		mNetwork.switchState(device.getFacility().getAdapterId(), device);
		mNetwork.updateFacility(facility.getAdapterId(), facility, EnumSet.allOf(SaveDevice.class));
		refreshFacility(facility, true);

		return true;
	}

}
