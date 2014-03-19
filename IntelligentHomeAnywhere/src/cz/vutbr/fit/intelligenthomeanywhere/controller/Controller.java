/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.controller;

import java.util.List;
import java.util.Random;

import android.content.Context;
import cz.vutbr.fit.intelligenthomeanywhere.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.DeviceLog;
import cz.vutbr.fit.intelligenthomeanywhere.household.DemoHousehold;
import cz.vutbr.fit.intelligenthomeanywhere.household.Household;
import cz.vutbr.fit.intelligenthomeanywhere.listing.FavoritesListing;
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
		if (mController == null)
			mController = new Controller(context);
		
		return mController;
	}

	public Controller(Context context) {
		mContext = context;

		mPersistence = new Persistence(mContext);
		mNetwork = new Network();
		mHousehold = demoMode ? new DemoHousehold(mContext) : new Household();
	}

	
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
	
	public Adapter getAdapter() {
		// FIXME: this method should be removed when there will be support for multiple adapters through whole application
		
		if (mHousehold.adapters == null) {
			// TODO: load adapters from network
			throw new NotImplementedException();
		}
		
		return mHousehold.adapters.get(0);
	}
	
	public List<Adapter> getAdapters() {
		if (mHousehold.adapters == null) {
			// TODO: load adapters from network
			throw new NotImplementedException();
		}
		
		return mHousehold.adapters;
	}
	
	public DeviceLog getDeviceLog(String id) {
		throw new NotImplementedException();
	}

	public List<String> getRooms(String adapterId) {
		throw new NotImplementedException();
	}
	
	public List<BaseDevice> getRoom(String roomId) {
		throw new NotImplementedException();
	}

	
	public List<String> getLists() {
		throw new NotImplementedException();
	}
	
	public List<BaseDevice> getList(String listId) {
		throw new NotImplementedException();
	}

	
	public BaseDevice getDevice(String id) {
		return getDevice(id, false);
	}
	
	public BaseDevice getDevice(String id, boolean forceUpdate) {
		BaseDevice device = null;
		
		for (Adapter adapter : mHousehold.adapters) {
			device = adapter.devices.getById(id);
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
	
	public BaseDevice saveDevice(BaseDevice device) {
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
	
	public User getUser(String id) {
		throw new NotImplementedException();
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
	
	public FavoritesListing getCustomList(String id) {
		throw new NotImplementedException();
	}

	
	public boolean registerAdapter(String id) {
		throw new NotImplementedException();
	}
	
	public boolean unregisterAdapter(String id) {
		throw new NotImplementedException();
	}

}