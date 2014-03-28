/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import cz.vutbr.fit.intelligenthomeanywhere.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.DeviceLog;
import cz.vutbr.fit.intelligenthomeanywhere.household.DemoHousehold;
import cz.vutbr.fit.intelligenthomeanywhere.household.Household;
import cz.vutbr.fit.intelligenthomeanywhere.listing.FavoritesListing;
import cz.vutbr.fit.intelligenthomeanywhere.listing.LocationListing;
import cz.vutbr.fit.intelligenthomeanywhere.network.Network;
import cz.vutbr.fit.intelligenthomeanywhere.persistence.Persistence;

public final class Controller {

	private static Controller mController;
	
	private final Context mContext;
	
	private final Persistence mPersistence;
	
	private final Network mNetwork;
	
	private final Household mHousehold;
	
	public static final boolean demoMode = true;
	
	public static Controller getInstance(Context context) {
		if (mController == null) {
			synchronized(Controller.class) {
				if (mController == null) {
					mController = new Controller(context);
				}
			}
		}
		
		return mController;
	}

	private Controller(Context context) {
		mContext = context;

		mPersistence = new Persistence(mContext);
		mNetwork = new Network();
		mHousehold = demoMode ? new DemoHousehold(mContext) : new Household();
	}

	
	/** Communication methods ***********************************************/
	
	public boolean login() {
		User lastUser = mPersistence.loadLastUser();
		
		if (lastUser != null)
			return login(lastUser.getEmail());
		
		return false; // TODO: throw proper exception
	}

	public boolean login(String userId) {
		if (!mNetwork.isAvailable())
			return false; // TODO: throw proper exception

		// TODO: catch and throw proper exception
		mHousehold.user = mNetwork.signIn(userId);
		if (mHousehold.user != null) {
			mPersistence.saveLastUser(mHousehold.user);
			return true;
		}
		
		return false;
	}
	
	public boolean logout() {
		mHousehold.user = null;
		mPersistence.saveLastUser(null);
		
		return true;
	}
	
	public boolean isLoggedIn() {
		return mHousehold.user != null;
	}
	
	
	/** Adapter methods *****************************************************/
	
	public List<Adapter> getAdapters() {
		if (mHousehold.adapters == null) {
			// TODO: load adapters from network
			throw new NotImplementedException();
		}
		
		return mHousehold.adapters;
	}
	
	public Adapter getAdapter(String adapterId) {
		for (Adapter a : getAdapters()) {
			if (a.getId().equals(adapterId))
				return a;
		}
		
		return null;
	}

	public boolean registerAdapter(String id) {
		throw new NotImplementedException();
	}

	public boolean unregisterAdapter(String id) {
		throw new NotImplementedException();
	}


	/** Location methods ****************************************************/
	
	public List<LocationListing> getLocations() {
		List<LocationListing> listings = new ArrayList<LocationListing>();
		
		for (Adapter adapter : getAdapters()) {
			for (String location : adapter.getLocations()) {
				listings.add(new LocationListing(location, location));
			}
		}
		
		return listings;
	}
	
	public LocationListing getLocation(String id) {
		throw new NotImplementedException();
	}
	
	public boolean addLocation(LocationListing location) {
		throw new NotImplementedException();
	}
	
	public boolean deleteLocation(LocationListing location) {
		throw new NotImplementedException();
	}
	
	public boolean saveLocation(LocationListing location) {
		throw new NotImplementedException();
	}
	
	public boolean renameLocation(String location, String newName) {
		// TODO: Use rather saveLocation() method
	
		for (Adapter adapter : getAdapters()) {
			for (BaseDevice device : adapter.getDevicesByLocation(location)) {
				device.setLocation(newName);
				// TODO: Save to server (somehow effectively)
			}
		}
		
		return true;
	}

	
	/** Devices methods *****************************************************/
	
	public BaseDevice getDevice(String id) {
		return getDevice(id, false);
	}
	
	public BaseDevice getDevice(String id, boolean forceUpdate) {
		BaseDevice device = null;
		
		for (Adapter adapter : getAdapters()) {
			device = adapter.getDeviceById(id);
			if (device != null)
				break;
		}

		boolean needsUpdate = true;
		// TODO: something like: needsUpdate = forceUpdate || !mPersistence->getDevice(device)
		
		if (device != null && needsUpdate) {
			// TODO: load actual value from network
			int i = new Random().nextInt(100);
			device.setValue(i);
		}
		
		return device;
	}
	
	public boolean hideDevice(BaseDevice device) {
		throw new NotImplementedException();
	}
	
	public boolean unhideDevice(BaseDevice device) {
		throw new NotImplementedException();
	}

	public List<BaseDevice> getUninitializedDevices() {
		List<BaseDevice> list = new ArrayList<BaseDevice>();
		
		for (Adapter adapter : getAdapters()) {
			list.addAll(adapter.getUninitializedDevices());
		}
		
		return list;
	}
	
	public List<BaseDevice> getDevicesByLocation(String location) {
		List<BaseDevice> list = new ArrayList<BaseDevice>();
		
		for (Adapter adapter : getAdapters()) {
			list.addAll(adapter.getDevicesByLocation(location));
		}
		
		return list;
	}
	
	/**
	 * Save specified setting of device to server.
	 * 
	 * @param device
	 * @param what type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveDevice(BaseDevice device, SaveDevice what) {
		return true;
		
		//throw new NotImplementedException();
	}
	
	/** Save all settings of device to server.
	 * 
	 * @param device
	 * @return true on success, false otherwise
	 */
	public boolean saveDevice(BaseDevice device) {
		return saveDevice(device, SaveDevice.SAVE_ALL);
	}
	
	public DeviceLog getDeviceLog(String id) {
		throw new NotImplementedException();
	}

	
	/** User methods ********************************************************/

	public User getUser(String id) {
		throw new NotImplementedException();
	}

	public boolean addUser(User user) {
		throw new NotImplementedException();
	}
	
	public boolean deleteUser(User user) {
		throw new NotImplementedException();
	}
	
	public boolean saveUser(User user) {
		throw new NotImplementedException();
	}
	
	
	/** Favorites lists methods *********************************************/
	
	public List<FavoritesListing> getCustomLists() {
		if (mHousehold.favoritesListings == null) {
			// TODO: load favorites from network
			throw new NotImplementedException();
		}
		
		return mHousehold.favoritesListings;
	}
	
	public FavoritesListing getCustomList(String id) {
		for (FavoritesListing l : getCustomLists()) {
			if (l.getId().equals(id))
				return l;
		}
		
		return null;
	}

	public boolean addCustomList(FavoritesListing list) {
		throw new NotImplementedException();
	}
	
	public boolean deleteCustomList(FavoritesListing list) {
		throw new NotImplementedException();
	}
	
	public boolean saveCustomList(FavoritesListing list) {
		throw new NotImplementedException();
	}

}