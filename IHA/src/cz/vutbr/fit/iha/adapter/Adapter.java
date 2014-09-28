/**
 * @brief Package for adapter manipulation
 */
package cz.vutbr.fit.iha.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.text.format.Time;
import android.util.Log;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.household.User;

/**
 * @brief Class for parsed data from XML file of adapters
 * @author ThinkDeep
 * 
 */
public class Adapter {
	public static final String TAG = Adapter.class.getSimpleName();

	private final Map<String, Location> mLocations = new HashMap<String, Location>();
	private final Map<String, Facility> mFacilities = new HashMap<String, Facility>();
	private final Map<String, Facility> mUninitializedFacilities = new HashMap<String, Facility>();
	private final Map<String, Facility> mUninitializedIgnored = new HashMap<String, Facility>();

	private String mId = "";
	private String mName = "";
	private User.Role mRole;
	private int mUtcOffset;

	public final Time lastUpdate = new Time();

	public Adapter() {
	}

	/**
	 * Debug method
	 */
	public String toDebugString() {
		String facilities = "";
		for (Facility dev : mFacilities.values()) {
			facilities += String.format(" - %s\n", dev.toDebugString());
		}

		return String.format("Id: %s\nName: %s\nRole: %s\nFacilities:\n%s", mId, mName, mRole, facilities);
	}

	/**
	 * Set name of adapter
	 * 
	 * @param name
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * Get name of adapter
	 * 
	 * @return
	 */
	public String getName() {
		return mName.length() > 0 ? mName : getId();
	}

	/**
	 * Set role of actual user of adapter
	 * 
	 * @param role
	 */
	public void setRole(User.Role role) {
		mRole = role;
	}

	/**
	 * Get role of actual user of adapter
	 * 
	 * @return
	 */
	public User.Role getRole() {
		return mRole;
	}

	/**
	 * Setting id of adapter
	 * 
	 * @param id
	 */
	public void setId(String id) {
		mId = id;
	}

	/**
	 * Returning id of adapter
	 * 
	 * @return id
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Setting utc offset of adapter
	 * 
	 * @param utcOffset in minutes
	 */
	public void setUtcOffset(int offset) {
		mUtcOffset = offset;
	}

	/**
	 * Returning id of adapter
	 * 
	 * @return utcOffset in minutes
	 */
	public int getUtcOffset() {
		return mUtcOffset;
	}

	/**
	 * Find and return facility by given id
	 * 
	 * @param id
	 *            of facility
	 * @return Facility or null if no facility with this id is found.
	 */
	public Facility getFacilityById(String id) {
		return mFacilities.get(id);
	}

	/**
	 * Return list of all facilities.
	 * 
	 * @return list with facilities (or empty map).
	 */
	public List<Facility> getFacilities() {
		return new ArrayList<Facility>(mFacilities.values());
	}

	/**
	 * Set facilities that belongs to this adapter. Also updates uninitialized and locations maps.
	 * 
	 * @param facilities
	 */
	public void setFacilities(final List<Facility> facilities) {
		clearFacilities();

		for (Facility facility : facilities) {
			addFacility(facility);
		}
	}

	/**
	 * Return list of locations.
	 * 
	 * @return list with locations (or empty list).
	 */
	public List<Location> getLocations() {
		return new ArrayList<Location>(mLocations.values());
	}

	/**
	 * Return location by id.
	 * 
	 * @param id
	 * @return Location if found, null otherwise.
	 */
	public Location getLocation(String id) {
		return mLocations.get(id);
	}

	/**
	 * Set locations that belongs to this adapter.
	 * 
	 * @param locations
	 */
	public void setLocations(final List<Location> locations) {
		mLocations.clear();

		for (Location location : locations) {
			addLocation(location);
		}
	}

	/**
	 * Adds new location to list of locations.
	 * 
	 * @param location
	 * @return false if there is already location with this id
	 */
	public boolean addLocation(Location location) {
		if (mLocations.containsKey(location.getId()))
			return false;

		mLocations.put(location.getId(), location);
		return true;
	}

	/**
	 * Removes location from list of locations.
	 * 
	 * @param id
	 * @return false when there wasn't location with this id
	 */
	public boolean deleteLocation(String id) {
		return mLocations.remove(id) != null;
	}

	/**
	 * Updates location in list of locations.
	 * 
	 * @param location
	 * @return
	 */
	public boolean updateLocation(Location location) {
		if (!mLocations.containsKey(location.getId())) {
			Log.w(TAG, String.format("Can't update location with id=%s. It doesn't exists.", location.getId()));
			return false;
		}

		mLocations.put(location.getId(), location);
		return true;
	}

	/**
	 * Return list of facilities in specified location.
	 * 
	 * @param id
	 * @return list with facilities (or empty list)
	 */
	public List<Facility> getFacilitiesByLocation(final String id) {
		List<Facility> facilities = new ArrayList<Facility>();

		// Small optimization
		if (!mLocations.containsKey(id))
			return facilities;

		for (Facility facilitz : mFacilities.values()) {
			if (facilitz.getLocationId().equals(id)) {
				facilities.add(facilitz);
			}
		}

		return facilities;
	}

	/**
	 * Returns list of all uninitialized facilities in this adapter
	 * 
	 * @return list with uninitialized facilities (or empty list)
	 */
	public List<Facility> getUninitializedFacilities() {
		return new ArrayList<Facility>(mUninitializedFacilities.values());
	}

	/**
	 * Add facility to this listing. Also updates uninitialized and locations maps.
	 * 
	 * @param facility
	 * @return
	 */
	public void addFacility(final Facility facility) {
		mFacilities.put(facility.getId(), facility);

		if (!mLocations.containsKey(facility.getLocationId()))
			Log.w(TAG, "Adding facility with unknown locationId: " + facility.getLocationId());

		if (!facility.isInitialized() && !mUninitializedIgnored.containsKey(facility.getId()))
			mUninitializedFacilities.put(facility.getId(), facility);
	}

	/**
	 * Refreshes facility in listings (e.g., in uninitialized facilities)
	 * 
	 * @param facility
	 * @return false if adapter doesn't contain this facility, true otherwise
	 */
	public boolean refreshFacility(final Facility facility) {
		if (!mFacilities.containsKey(facility.getId())) {
			Log.w(TAG, String.format("Can't refresh facility '%s', adapter '%s' doesn't contain it", facility.getId(), getName()));
			return false;
		}

		if (facility.isInitialized()) {
			if (mUninitializedFacilities.remove(facility.getId()) != null)
				Log.d(TAG, "Removing initialized facility " + facility.toString());
		} else {
			mUninitializedFacilities.put(facility.getId(), facility);
			Log.d(TAG, "Adding uninitialized facility " + facility.toString());
		}
		return true;
	}

	/**
	 * Clears all facilities, uninitialized facilities and locations maps.
	 */
	private void clearFacilities() {
		mFacilities.clear();
		mUninitializedFacilities.clear();
	}

	public void ignoreUninitialized(List<Facility> facilities) {
		// TODO: save this list into some cache
		for (Facility facility : facilities) {
			String id = facility.getId();

			if (mUninitializedFacilities.containsKey(id)) {
				if (!mUninitializedIgnored.containsKey(id))
					mUninitializedIgnored.put(id, mUninitializedFacilities.get(id));

				mUninitializedFacilities.remove(id);
			}
		}
	}

	public void unignoreUninitialized() {
		// TODO: update that list in some cache
		for (Facility facility : mUninitializedIgnored.values()) {
			String id = facility.getId();

			if (!mUninitializedFacilities.containsKey(id))
				mUninitializedFacilities.put(id, mUninitializedIgnored.get(id));
		}

		mUninitializedIgnored.clear();
	}

	/**
	 * This is used ONLY for DemoMode when saving new location!
	 * 
	 * @return unique id of location
	 */
	public String getUnusedLocationId() {
		String id;
		Random random = new Random();

		do {
			id = String.valueOf(random.nextInt(1000));
		} while (mLocations.containsKey(id));

		return id;
	}

	public boolean isEmpty() {
		return mLocations.size() == 0 && mFacilities.size() == 0;
	}

}
