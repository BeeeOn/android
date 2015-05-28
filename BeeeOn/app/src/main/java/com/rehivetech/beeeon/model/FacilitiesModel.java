package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;


public class FacilitiesModel extends BaseModel {

	private static final String TAG = FacilitiesModel.class.getSimpleName();

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final MultipleDataHolder<Facility> mFacilities = new MultipleDataHolder<>(); // adapterId => facility dataHolder

	public FacilitiesModel(INetwork network) {
		super(network);
	}

	/**
	 * Return facility by ID.
	 *
	 * @param id
	 * @return facility or null if no facility is found
	 */
	public Facility getFacility(String adapterId, String id) {
		return mFacilities.getObject(adapterId, id);
	}

	/**
	 * Return module by ID
	 *
	 * @param adapterId
	 * @param id
	 * @return
	 */
	public Module getModule(String adapterId, String id) {
		String[] ids = id.split(Module.ID_SEPARATOR, 2);

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

		ModuleType type = ModuleType.fromTypeId(iType);

		return facility.getModuleByType(type, offset);
	}

	/**
	 * Return list of all facilities from adapter
	 *
	 * @param adapterId
	 * @return List of facilities (or empty list)
	 */
	public List<Facility> getFacilitiesByAdapter(String adapterId) {
		List<Facility> facilities = mFacilities.getObjects(adapterId);

		// Sort result facilities by id
		Collections.sort(facilities, new IdentifierComparator());

		return facilities;
	}

	/**
	 * Return list of all facilities by location from adapter
	 *
	 * @param locationId
	 * @return List of facilities (or empty list)
	 */
	public List<Facility> getFacilitiesByLocation(String adapterId, String locationId) {
		List<Facility> facilities = new ArrayList<>();

		for (Facility facility : getFacilitiesByAdapter(adapterId)) {
			if (facility.getLocationId().equals(locationId)) {
				facilities.add(facility);
			}
		}

		return facilities;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadFacilitiesByAdapter(String adapterId, boolean forceReload) throws AppException {
		if (!forceReload && !mFacilities.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mFacilities.setObjects(adapterId, mNetwork.initAdapter(adapterId));
		mFacilities.setLastUpdate(adapterId, DateTime.now());

		return true;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param facilities
	 * @return
	 */
	public boolean refreshFacilities(List<Facility> facilities, boolean forceReload) throws AppException {
		// Remove not expired facilities

		// NOTE: changed from foreach version to this -> http://stackoverflow.com/questions/1196586/calling-remove-in-foreach-loop-in-java
		Iterator<Facility> facIterator = facilities.iterator();
		while (facIterator.hasNext()) {
			Facility facility = facIterator.next();
			if (!forceReload && !facility.isExpired()) {
				facIterator.remove();
			}
		}

		List<Facility> newFacilities = mNetwork.getFacilities(facilities);
		if (newFacilities == null)
			return false;

		for (Facility newFacility : newFacilities) {
			mFacilities.addObject(newFacility.getAdapterId(), newFacility);
		}

		return true;
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

		mFacilities.addObject(facility.getAdapterId(), facility);

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
	public boolean saveFacility(Facility facility, EnumSet<SaveModule> what) throws AppException {
		mNetwork.updateFacility(facility.getAdapterId(), facility, what);
		refreshFacility(facility, true);

		return true;
	}

	/**
	 * Delete facility from server.
	 *
	 * This CAN'T be called on UI thread!
	 */
	public boolean deleteFacility(Facility facility) throws AppException {
		if (mNetwork.deleteFacility(facility)) {
			// Facility was deleted on server, remove it from map too
			mFacilities.removeObject(facility.getAdapterId(), facility.getId());
			return true;
		}

		return false;
    }

	/**
	 * Save specified settings of module to server.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param module
	 * @param what
	 *            type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveModule(Module module, EnumSet<SaveModule> what) throws AppException {
		Facility facility = module.getFacility();

		mNetwork.updateModule(facility.getAdapterId(), module, what);
		refreshFacility(facility, true);

		return true;
	}

	/**
	 * Send request to server to switch Actor value.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param module
	 *            ModuleType of this module must be actor, i.e., module.getType().isActor() must return true.
	 * @return true on success, false otherwise
	 */
	public boolean switchActor(Module module) throws AppException {
		if (!module.getType().isActor()) {
			Log.e(TAG, String.format("Tried to switch NOT-actor module '%s'", module.getName()));
			return false;
		}
		
		Facility facility = module.getFacility();

		mNetwork.switchState(module.getFacility().getAdapterId(), module);
		refreshFacility(facility, true);

		return true;
	}

}
