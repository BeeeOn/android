package com.rehivetech.beeeon.persistence;

import android.content.Context;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.network.INetwork;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationsModel {

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final INetwork mNetwork;
	private final Context mContext;

	private final Map<String, DataHolder<Location>> mLocations = new HashMap<>(); // adapterId => location dataHolder

	public LocationsModel(INetwork network, Context context) {
		mNetwork = network;
		mContext = context;
	}

	private Location createNoLocation(String adapterId) {
		return new Location(Location.NO_LOCATION_ID, mContext.getString(R.string.loc_none), adapterId, Location.NO_LOCATION_TYPE);
	}

	/**
	 * Return location from active adapter by id.
	 *
	 * @param id
	 * @return Location if found, null otherwise.
	 */
	public Location getLocation(String adapterId, String id) {
		// Support "no location"
		if (id.equals(Location.NO_LOCATION_ID)) {
			return createNoLocation(adapterId);
		}

		DataHolder<Location> adapterLocations = mLocations.get(adapterId);
		if (adapterLocations == null) {
			return null;
		}

		return adapterLocations.getObject(id);
	}

	/**
	 * Return list of locations from active adapter.
	 *
	 * @return List of locations (or empty list)
	 */
	public List<Location> getLocationsByAdapter(String adapterId) {
		DataHolder<Location> adapterLocations = mLocations.get(adapterId);
		if (adapterLocations == null) {
			return new ArrayList<Location>();
		}

		List<Location> locations = adapterLocations.getObjects();

		// Sort result locations by name, id
		Collections.sort(locations, new NameIdentifierComparator());

		// Add "no location" for devices without location
		locations.add(createNoLocation(adapterId));

		return locations;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadLocationsByAdapter(String adapterId, boolean forceReload) {
		DataHolder<Location> adapterLocations = mLocations.get(adapterId);

		if (adapterLocations == null) {
			adapterLocations = new DataHolder<>();
			mLocations.put(adapterId, adapterLocations);
		}

		if (!forceReload && !adapterLocations.isExpired(RELOAD_EVERY_SECONDS)) {
			return false;
		}

		adapterLocations.setObjects(mNetwork.getLocations(adapterId));
		adapterLocations.setLastUpdate(DateTime.now());

		return true;
	}

	private void updateLocationInMap(Location location) {
		String adapterId = location.getAdapterId();

		DataHolder<Location> adapterLocations = mLocations.get(adapterId);

		if (adapterLocations == null) {
			adapterLocations = new DataHolder<>();
			mLocations.put(adapterId, adapterLocations);
		}

		adapterLocations.addObject(location);
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
			DataHolder<Location> adapterLocations = mLocations.get(location.getAdapterId());
			if (adapterLocations != null)
				adapterLocations.removeObject(location.getId());
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
