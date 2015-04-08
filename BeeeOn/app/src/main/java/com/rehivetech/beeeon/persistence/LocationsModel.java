package com.rehivetech.beeeon.persistence;

import android.content.Context;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.network.INetwork;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationsModel {

	private final INetwork mNetwork;
	private final Context mContext;

	private final Map<String, Map<String, Location>> mLocations = new HashMap<String, Map<String, Location>>(); // adapterId => (locationId => location)
	private final Map<String, DateTime> mLastUpdates = new HashMap<String, DateTime>(); // adapterId => lastUpdate of location

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	public LocationsModel(INetwork network, Context context) {
		mNetwork = network;
		mContext = context;
	}

	/**
	 * Return location from active adapter by id.
	 *
	 * @param id
	 * @return Location if found, null otherwise.
	 */
	public Location getLocation(String adapterId, String id) {
		Map<String, Location> adapterLocations = mLocations.get(adapterId);
		if (adapterLocations == null) {
			return null;
		}

		return adapterLocations.get(id);
	}

	/**
	 * Return list of locations from active adapter.
	 *
	 * @return List of locations (or empty list)
	 */
	public List<Location> getLocationsByAdapter(String adapterId) {
		List<Location> locations = new ArrayList<Location>();

		Map<String, Location> adapterLocations = mLocations.get(adapterId);
		if (adapterLocations != null) {
			for (Location location : adapterLocations.values()) {
				locations.add(location);
			}
		}

		// Sort result locations by id
		Collections.sort(locations, new NameIdentifierComparator());

		// Add "no location" for devices without location
		Location noneLocation = new Location(Location.NO_LOCATION_ID, mContext.getString(R.string.loc_none), adapterId, Location.NO_LOCATION_TYPE);
		locations.add(noneLocation);

		return locations;
	}

	private void setLocationsByAdapter(String adapterId, List<Location> locations) {
		Map<String, Location> adapterLocations = mLocations.get(adapterId);
		if (adapterLocations != null) {
			adapterLocations.clear();
		} else {
			adapterLocations = new HashMap<String, Location>();
			mLocations.put(adapterId, adapterLocations);
		}

		for (Location location : locations) {
			adapterLocations.put(location.getId(), location);
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
	public synchronized boolean reloadLocationsByAdapter(String adapterId, boolean forceReload) {
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

	private boolean loadFromServer(String adapterId) throws AppException {
		setLocationsByAdapter(adapterId, mNetwork.getLocations(adapterId));
		setLastUpdate(adapterId, DateTime.now());
		saveToCache(adapterId);

		return true;
	}

	private boolean loadFromCache(String adapterId) {
		// TODO: implement this
		return false;

		// setLocationsByAdapter(locationsFromCache);
		// setLastUpdate(adapterId, lastUpdateFromCache);
	}

	private void saveToCache(String adapterId) {
		// TODO: implement this
	}

	private void updateLocationInMap(Location location) {
		String adapterId = location.getAdapterId();

		Map<String, Location> adapterLocations = mLocations.get(adapterId);
		if (adapterLocations == null) {
			adapterLocations = new HashMap<String, Location>();
			mLocations.put(adapterId, adapterLocations);
		}

		adapterLocations.put(location.getId(), location);
	}

	/**
	 * Save changed location to server and update it in list of locations.
	 *
	 * @param location
	 * @return
	 */
	public boolean updateLocation(Location location) {
		if (mNetwork.updateLocation(location)) {
			// Location was updated on server, update it in map too
			updateLocationInMap(location);
			return true;
		}

		return false;
	}

	/**
	 * Deletes location from server and from list of locations.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param location
	 * @return
	 */
	public boolean deleteLocation(Location location) {
		if (mNetwork.deleteLocation(location)) {
			// Location was deleted on server, remove it from map too
			Map<String, Location> adapterLocations = mLocations.get(location.getAdapterId());
			if (adapterLocations != null)
				adapterLocations.remove(location.getId());
			return true;
		}

		return false;
	}

	/**
	 * Create and add new location to server and to list of locations.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param location
	 * @return Location on success, null otherwise
	 */
	public Location createLocation(Location location) {
		Location newLocation = mNetwork.createLocation(location);
		if (newLocation != null) {
			// Location was updated on server, update it in map too
			updateLocationInMap(newLocation);
		}

		return newLocation;
	}

}
