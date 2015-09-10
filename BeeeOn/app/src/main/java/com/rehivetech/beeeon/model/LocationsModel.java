package com.rehivetech.beeeon.model;

import android.content.Context;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class LocationsModel extends BaseModel {

	private static int RELOAD_EVERY_SECONDS = 10 * 60;

	private final String mNoLocationName;

	private final MultipleDataHolder<Location> mLocations = new MultipleDataHolder<>(); // gateId => location dataHolder

	public LocationsModel(INetwork network, Context context,Integer reloadEvery) {
		super(network);
		mNoLocationName = context.getString(R.string.loc_none);
		RELOAD_EVERY_SECONDS = reloadEvery;
	}

	private Location createNoLocation(String gateId) {
		return new Location(Location.NO_LOCATION_ID, mNoLocationName, gateId, Location.NO_LOCATION_TYPE);
	}

	/**
	 * Return location from active gate by id.
	 *
	 * @param id
	 * @return Location if found, null otherwise.
	 */
	public Location getLocation(String gateId, String id) {
		// Support "no location"
		if (id.equals(Location.NO_LOCATION_ID)) {
			return createNoLocation(gateId);
		}

		return mLocations.getObject(gateId, id);
	}

	/**
	 * Return list of locations from active gate.
	 *
	 * @return List of locations (or empty list)
	 */
	public List<Location> getLocationsByGate(String gateId) {
		List<Location> locations = mLocations.getObjects(gateId);

		// Sort result locations by name, id
		Collections.sort(locations, new NameIdentifierComparator());

		// Add "no location" for devices without location
		locations.add(createNoLocation(gateId));

		return locations;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadLocationsByGate(String gateId, boolean forceReload) {
		if (!forceReload && !mLocations.isExpired(gateId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mLocations.setObjects(gateId, mNetwork.getLocations(gateId));
		mLocations.setLastUpdate(gateId, DateTime.now());

		return true;
	}

	/**
	 * Save changed location to server and update it in list of locations.
	 *
	 * @param location
	 * @return
	 */
	public boolean updateLocation(Location location) {
		String gateId = location.getGateId();

		if (mNetwork.updateLocation(location)) {
			// Location was updated on server, update it in map too
			mLocations.addObject(gateId, location);
			return true;
		}

		return false;
	}

	/**
	 * Deletes location from server and from list of locations.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param location
	 * @return
	 */
	public boolean deleteLocation(Location location) {
		String gateId = location.getGateId();
		String locationId = location.getId();

		if (mNetwork.deleteLocation(location)) {
			// Location was deleted on server, remove it from map too
			mLocations.removeObject(gateId, locationId);
			return true;
		}

		return false;
	}

	/**
	 * Create and add new location to server and to list of locations.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param location
	 * @return Location on success, null otherwise
	 */
	public Location createLocation(Location location) {
		String gateId = location.getGateId();

		Location newLocation = mNetwork.createLocation(location);
		if (newLocation != null) {
			// Location was updated on server, update it in map too
			mLocations.addObject(gateId, newLocation);
		}

		return newLocation;
	}

}
