package cz.vutbr.fit.iha.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.format.Time;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.network.exception.NetworkException;
import cz.vutbr.fit.iha.util.Utils;

public class AdaptersModel {
	
	private final Network mNetwork;
	
	private final Map<String, Adapter> mAdapters = new HashMap<String, Adapter>();
	
	private final Time mLastUpdate = new Time();
	
	private static final long RELOAD_EVERY_SECONDS = 10 * 60;
	
	public AdaptersModel(Network network) {
		mNetwork = network;
	}
	
	public Map<String, Adapter> getAdaptersMap() {
		return mAdapters;
	}
	
	public List<Adapter> getAdapters() {
		List<Adapter> adapters = new ArrayList<Adapter>();
		
		for (Adapter adapter : mAdapters.values()) {
			adapters.add(adapter);
		}
		
		return adapters;
	}
	
	public void setAdapters(List<Adapter> adapters) {
		mAdapters.clear();

		for (Adapter adapter : adapters) {
			mAdapters.put(adapter.getId(), adapter);
		}
	}
	
	public Adapter getAdapter(String id) {
		return mAdapters.get(id);
	}
	
	private void setLastUpdate(Time lastUpdate) {
		if (lastUpdate == null)
			mLastUpdate.setToNow();
		else
			mLastUpdate.set(lastUpdate);
	}
	
	private boolean isExpired() {
		return Utils.isExpired(mLastUpdate, RELOAD_EVERY_SECONDS);
	}
	
	public boolean reloadAdapters(boolean forceReload) {
		if (!forceReload && !isExpired()) {
			return false;
		}

		// TODO: check if user is logged in
		if (mNetwork.isAvailable()) {
			return loadFromServer();
		} else if (forceReload) {
			return loadFromCache();
		}
		
		return false;
	}
	
	private boolean loadFromServer() {
		try {
			setAdapters(mNetwork.getAdapters());
			setLastUpdate(null);
			saveToCache();
		} catch (NetworkException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean loadFromCache() {
		// TODO: implement this
		return false;
		
		//setAdapters(adaptersFromCache);
		//setLastUpdate(lastUpdateFromCache);
	}
	
	private void saveToCache() {
		// TODO: implement this
	}

}
