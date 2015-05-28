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

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final String mNoLocationName;

	private final MultipleDataHolder<Location> mLocations = new MultipleDataHolder<>(); // adapterId => location dataHolder

	public LocationsModel(INetwork network, Context context) {
		super(network);
		mNoLocationName = context.getString(R.string.loc_none);
	}

	private Location createNoLocation(String adapterId) {
		return new Location(Location.NO_LOCATION_ID, mNoLocationName, adapterId, Location.NO_LOCATION_TYPE);
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

		return mLocations.getObject(adapterId, id);
	}

	/**
	 * Return list of locations from active adapter.
	 *
	 * @return List of locations (or empty list)
	 */
	public List<Location> getLocationsByAdapter(String adapterId) {
		List<Location> locations = mLocations.getObjects(adapterId);

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
		if (!forceReload && !mLocations.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mLocations.setObjects(adapterId, mNetwork.getLocations(adapterId));
		mLocations.setLastUpdate(adapterId, DateTime.now());

		return true;
	}

	/**
	 * Save changed location to server and update it in list of locations.
	 *
	 * @param location
	 * @return
	 */
	public boolean updateLocation(Location location) {
		String adapterId = location.getAdapterId();

		if (mNetwork.updateLocation(location)) {
			// Location was updated on server, update it in map too
			mLocations.addObject(adapterId, location);
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
		String adapterId = location.getAdapterId();
		String locationId = location.getId();

		if (mNetwork.deleteLocation(location)) {
			// Location was deleted on server, remove it from map too
			mLocations.removeObject(adapterId, locationId);
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
		String adapterId = location.getAdapterId();

		Location newLocation = mNetwork.createLocation(location);
		if (newLocation != null) {
			// Location was updated on server, update it in map too
			mLocations.addObject(adapterId, newLocation);
		}

		return newLocation;
	}

}
