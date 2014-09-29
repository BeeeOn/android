package cz.vutbr.fit.iha.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.text.format.Time;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.network.exception.NetworkException;
import cz.vutbr.fit.iha.util.Utils;

public class UninitializedFacilitiesModel {
	
	private final Network mNetwork;
	
	private final Map<String, Map<String, Facility>> mFacilities = new HashMap<String, Map<String, Facility>>(); // adapterId => (facilityId => facility)
	private final Map<String, Time> mLastUpdates = new HashMap<String, Time>(); // adapterId => lastUpdate of facilities
	private final Vector<String> mIgnoredFacilities = new Vector<String>(); // adapterId
	
	private static final long RELOAD_EVERY_SECONDS = 10 * 60;
	
	public UninitializedFacilitiesModel(Network network) {
		mNetwork = network;
	}
	
	public List<Facility> getUninitializedFacilitiesByAdapter(String adapterId, boolean withIgnored) {
		List<Facility> facilities = new ArrayList<Facility>();
		
		Map<String, Facility> adapterFacilities = mFacilities.get(adapterId);
		if (adapterFacilities != null) {
			for (Facility facility : adapterFacilities.values()) {
				if (facility.getAdapterId().equals(adapterId)) {
					if (withIgnored || !mIgnoredFacilities.contains(facility.getId())) {
						facilities.add(facility);
					}
				}
			}	
		}
		
		return facilities;
	}
	
	public void ignoreUninitalizedFacilities(String adapterId) {
		for (Facility facility : getUninitializedFacilitiesByAdapter(adapterId, false)) {
			mIgnoredFacilities.add(facility.getId());	
		}
	}
	
	public void unignoreUninitializedFacilities(String adapterId) {
		for (Facility facility : getUninitializedFacilitiesByAdapter(adapterId, true)) {
			mIgnoredFacilities.removeElement(facility.getId());
		}
	}
	
	public void setUninitialiyedFacilitiesByAdapter(String adapterId, List<Facility> facilities) {
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
	
	private void setLastUpdate(String adapterId, Time lastUpdate) {
		Time oldLastUpdate = mLastUpdates.get(adapterId);
		if (oldLastUpdate == null) {
			oldLastUpdate = new Time();
			mLastUpdates.put(adapterId, oldLastUpdate);
		}

		if (lastUpdate == null)
			oldLastUpdate.setToNow();
		else
			oldLastUpdate.set(lastUpdate);
	}
	
	private boolean isExpired(String adapterId) {
		return !mLastUpdates.containsKey(adapterId) || Utils.isExpired(mLastUpdates.get(adapterId), RELOAD_EVERY_SECONDS);
	}
	
	public boolean reloadUninitializedFacilitiesByAdapter(String adapterId, boolean forceReload) {
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
			// TODO: Load ignoredUninitializedDevices from some cache
			setUninitialiyedFacilitiesByAdapter(adapterId, mNetwork.getNewFacilities(adapterId));
			setLastUpdate(adapterId, null);
			saveToCache(adapterId);
		} catch (NetworkException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean loadFromCache(String adapterId) {
		// TODO: implement this
		return false;
		
		// TODO: Load ignoredUninitializedDevices from some cache
		//setFacilitiesByAdapter(facilitiesFromCache);
		//setLastUpdate(adapterId, lastUpdateFromCache);
	}
	
	private void saveToCache(String adapterId) {
		// TODO: implement this
		// TODO: Save also ignoredUninitializedDevices to some cache
	}

}
