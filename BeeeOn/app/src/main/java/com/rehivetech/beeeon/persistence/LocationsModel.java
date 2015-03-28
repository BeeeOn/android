package com.rehivetech.beeeon.persistence;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.network.INetwork;

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

	public Location getLocation(String adapterId, String id) {
		Map<String, Location> adapterLocations = mLocations.get(adapterId);
		if (adapterLocations == null) {
			return null;
		}

		return adapterLocations.get(id);
	}

	public List<Location> getLocationsByAdapter(String adapterId) {
		List<Location> locations = new ArrayList<Location>();

		Map<String, Location> adapterLocations = mLocations.get(adapterId);
		if (adapterLocations != null) {
			for (Location location : adapterLocations.values()) {
				locations.add(location);
			}
		}

		// Sort result locations by id
		Collections.sort(locations, new IdentifierComparator());

		// Add "no location" for devices without location
		Location noneLocation = new Location(Location.NO_LOCATION_ID, mContext.getString(R.string.loc_none), Location.NO_LOCATION_TYPE);
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

	public boolean reloadLocationsByAdapter(String adapterId, boolean forceReload) {
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
			setLocationsByAdapter(adapterId, mNetwork.getLocations(adapterId));
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

		// setLocationsByAdapter(locationsFromCache);
		// setLastUpdate(adapterId, lastUpdateFromCache);
	}

	private void saveToCache(String adapterId) {
		// TODO: implement this
	}

	/**
	 * Updates location in list of locations.
	 * 
	 * @param location
	 * @return
	 */
	public boolean updateLocation(String adapterId, Location location) {
		// TODO: review and refactor, this is just copied from Adapter
		Map<String, Location> adapterLocations = mLocations.get(adapterId);

		// TODO: check/create adapterLocations object
		if (!adapterLocations.containsKey(location.getId())) {
			// //Log.w(TAG, String.format("Can't update location with id=%s. It doesn't exists.", location.getId()));
			return false;
		}

		adapterLocations.put(location.getId(), location);
		return true;

	}

	/**
	 * Removes location from list of locations.
	 * 
	 * @param id
	 * @return false when there wasn't location with this id
	 */
	public boolean deleteLocation(String adapterId, String id) {
		// TODO: review and refactor, this is just copied from Adapter
		// TODO: check/create adapterLocations object
		Map<String, Location> adapterLocations = mLocations.get(adapterId);
		return adapterLocations.remove(id) != null;
	}

	/**
	 * Adds new location to list of locations.
	 * 
	 * @param location
	 * @return false if there is already location with this id
	 */
	public boolean addLocation(String adapterId, Location location) {
		// TODO: review and refactor, this is just copied from Adapter
		Map<String, Location> adapterLocations = mLocations.get(adapterId);

		// TODO: check/create adapterLocations object
		if (adapterLocations.containsKey(location.getId()))
			return false;

		adapterLocations.put(location.getId(), location);
		return true;
	}

}
