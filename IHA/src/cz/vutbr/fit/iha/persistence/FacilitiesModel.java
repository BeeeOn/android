package cz.vutbr.fit.iha.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.format.Time;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.network.exception.NetworkException;
import cz.vutbr.fit.iha.network.xml.UtcAdapterPair;
import cz.vutbr.fit.iha.util.Utils;

public class FacilitiesModel {
	
	private final Network mNetwork;
	
	private final Map<String, Map<String, Facility>> mFacilities = new HashMap<String, Map<String, Facility>>(); // adapterId => (facilityId => facility)
	private final Map<String, Time> mLastUpdates = new HashMap<String, Time>(); // adapterId => lastUpdate of facilities
	
	private static final long RELOAD_EVERY_SECONDS = 10 * 60;
	
	public FacilitiesModel(Network network) {
		mNetwork = network;
	}
	
	public Facility getFacility(String adapterId, String id) {
		Map<String, Facility> adapterFacilities = mFacilities.get(adapterId);
		if (adapterFacilities == null) {
			return null;
		}
		
		return adapterFacilities.get(id);		
	}
	
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
		
		return facilities;
	}
	
	public void setFacilitiesByAdapter(String adapterId, List<Facility> facilities) {
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
	
	public List<Facility> getFacilitiesByLocation(String adapterId, String locationId) {
		List<Facility> facilities = new ArrayList<Facility>();
		
		for (Facility facility : getFacilitiesByAdapter(adapterId)) {
			if (facility.getLocationId().equals(locationId)) {
				facilities.add(facility);
			}
		}
		
		return facilities;
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
	
	public boolean reloadFacilitiesByAdapter(String adapterId, boolean forceReload) {
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
	
	// TODO: implement method for only refreshing facilities data
	// public boolean refreshFacilitiesByLocation(String adapterId, String locationId, boolean forceRefresh) {}
	// public boolean refreshFacility(String adapterId, String locationId, boolean forceRefresh) {}
	
	private boolean loadFromServer(String adapterId) {
		try {
			UtcAdapterPair pair = mNetwork.init(adapterId);
			setFacilitiesByAdapter(adapterId, pair.Facilities);
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
		
		//setFacilitiesByAdapter(facilitiesFromCache);
		//setLastUpdate(adapterId, lastUpdateFromCache);
	}
	
	private void saveToCache(String adapterId) {
		// TODO: implement this
	}

	public boolean refreshFacility(Facility facility) {
		// FIXME: needs review and refactoring... it is just copied from Controller
		
		try {
			Facility newFacility = mNetwork.getFacility(facility.getAdapterId(), facility);
			if (newFacility == null)
				return false;

			facility.replaceData(newFacility);
		} catch (NetworkException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
