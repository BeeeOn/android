package cz.vutbr.fit.iha.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.Time;
import cz.vutbr.fit.iha.User;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.StateDevice;
import cz.vutbr.fit.iha.adapter.device.SwitchDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.exception.NetworkException;
import cz.vutbr.fit.iha.exception.NoConnectionException;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.household.DemoHousehold;
import cz.vutbr.fit.iha.household.Household;
import cz.vutbr.fit.iha.listing.FavoritesListing;
import cz.vutbr.fit.iha.listing.Location;
import cz.vutbr.fit.iha.listing.LocationListing;
import cz.vutbr.fit.iha.network.ActualUser;
import cz.vutbr.fit.iha.network.GetGoogleAuth;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.persistence.Persistence;

/**
 * Core of application (used as singleton), provides methods and access to all data and household.
 *
 * @author Robyer
 */
public final class Controller {

	/** This singleton instance. */
	private static Controller mController;
	
	/** Application context */
	private final Context mContext;
	
	/** Persistence service for caching purposes */
	private final Persistence mPersistence;
	
	/** Network service for communication with server */
	private final Network mNetwork;
	
	/** Household object holds logged in user and all adapters and lists which belongs to him */
	private Household mHousehold;
	
	/** Switch for using demo mode (with example adapter, without server) */
	private static boolean mDemoMode = false;
	
	/** When set to true (by calling {@link #reloadAdapters()}), it will reload all adapters from server in next call of {@link #getAdapters()} */
	private boolean mReloadAdapters = true;
	
	/**
	 * Return singleton instance of this Controller.
	 * This is thread-safe.
	 * 
	 * @param global application context
	 * @return singleton instance of controller
	 */
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

	/**
	 * Private constructor.
	 * 
	 * @param global application context
	 */
	private Controller(Context context) {
		mContext = context;

		mPersistence = new Persistence(mContext);
		mNetwork = new Network(mContext);
		try {
			mHousehold = mDemoMode ? new DemoHousehold(mContext) : new Household();
		} catch (Exception e) {
			mHousehold = new Household();
			e.printStackTrace();
		}
	}
	
	public static synchronized void setDemoMode(Context context, boolean demoMode) {
		if (mDemoMode == demoMode)
			return;
		
		mDemoMode = demoMode;
		mController = new Controller(context);
	}

	
	/** Communication methods ***********************************************/
	
	public String getLastEmail() {
		return mPersistence.loadLastEmail();
	}

	/**
	 * Login user by his id (authenticate on server).
	 * 
	 * @param userId
	 * @return true on success, false otherwise
	 * @throws NetworkException
	 */
	public boolean login(String email) throws NetworkException {
		if (mDemoMode)
			return true;
		
		if (!mNetwork.isAvailable())
			throw new NoConnectionException();

		// TODO: catch and throw proper exception
		// FIXME: after some time there should be picture in ActualUser object, should save to mPersistence
		mHousehold.user = mNetwork.signIn(email);
		if (mHousehold.user != null) {
			mPersistence.saveLastEmail(mHousehold.user.getEmail());
			return true;
		}
		
		return false;
	}

	/**
	 * Logout user from application (and forget him as last user).
	 * 
	 * @return true always
	 */
	public boolean logout() {
		// TODO: also destroy session
		mHousehold.user = null;
		mPersistence.saveLastEmail(null);
		
		return true;
	}

	/**
	 * Checks if user is logged in (with valid session).
	 * 
	 * @return true if user is logged in, false otherwise
	 */
	public boolean isLoggedIn() {
		// TODO: also check session lifetime
		return mHousehold.user != null;
	}
	
	
	/** Adapter methods *****************************************************/

	private boolean refreshAdapter(Adapter adapter, boolean forceUpdate) {
		if (mDemoMode)
			return true;
		
		Time that = new Time();
		that.setToNow();
		that.set(that.toMillis(true) - 30000); // 30 seconds interval between updates
		
		// Update only when needed
		if (!forceUpdate && !adapter.lastUpdate.before(that))
			return false;

		Adapter newAdapter = null;
		
		try {
			newAdapter = mNetwork.init(adapter.getId());
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		if (newAdapter == null)
			return false;
		
		// Update adapter with new data
		adapter.setDevices(newAdapter.getDevices());
		adapter.setId(newAdapter.getId());
		adapter.setName(newAdapter.getName());
		adapter.setVersion(newAdapter.getVersion());
		adapter.setRole(newAdapter.getRole());		
		
		adapter.lastUpdate.setToNow();
		
		return true;
	}
	
	/**
	 * Calling this will reload all adapters from server in next call of {@link #getAdapters()}
	 */
	public void reloadAdapters() {
		mReloadAdapters = true;
	}
	
	/**
	 * Refreshes device in listings (e.g., in uninitialized devices)
	 * @param device
	 */
	public void refreshDevice(final BaseDevice device) {
		for (Adapter adapter : mHousehold.adapters) {
			adapter.refreshDevice(device);
		}
	}
	
	public boolean updateDevice(BaseDevice device) {
		if (mDemoMode) {
			// In demo mode update device with random value 
			if (device instanceof SwitchDevice) {
				((SwitchDevice)device).setActive(!((SwitchDevice)device).isActive());
			} else if (device instanceof StateDevice) {
				((StateDevice)device).setActive(!((StateDevice)device).isActive());
			} else {
				int i = new Random().nextInt(100);
				device.setValue(i);
			}
			return true;
		}
		
		ArrayList<BaseDevice> devices = new ArrayList<BaseDevice>();
		devices.add(device);

		try {
			devices = mNetwork.update(devices);
			if (devices == null || devices.size() != 1)
				return false;
			
			BaseDevice newDevice = devices.get(0);
			device.setLocation(newDevice.getLocation());
			device.setName(newDevice.getName());
			device.setRefresh(newDevice.getRefresh());
			device.lastUpdate.set(newDevice.lastUpdate);
			//device.setLogging(newDevice.getLogging());
			//device.setValue(newDevice.getValue());
			// TODO: all other values etc.
		} catch (NetworkException e) {
			e.printStackTrace();
		}
				
		refreshDevice(device);
		return true;
	}
	
	/**
	 * Return all adapters that this logged in user has access to.
	 * 
	 * @return List of adapters
	 */
	public List<Adapter> getAdapters() {
		// TODO: refactor this method, make household's adapters (and favoriteslisting, and user?) final etc.
		if (!mDemoMode && (mHousehold.adapters == null || mReloadAdapters)) { 
			try { 
				mHousehold.adapters = mNetwork.getAdapters();
			} catch (NetworkException e) {
				// TODO Auto-generated catch block 
				e.printStackTrace(); 
			}
			mReloadAdapters = false;
		}

		// Network or another error, we must return correct object now, but adapters must be loaded later
		if (mHousehold.adapters == null) 
			return new ArrayList<Adapter>();

		// Refresh all adapters (load their devices)
		for (Adapter adapter : mHousehold.adapters) {
			refreshAdapter(adapter, false);
		}
		
		return mHousehold.adapters;
	}
	
	/**
	 * Return adapter by his ID.
	 * 
	 * @param id
	 * @return Adapter if found, null otherwise
	 */
	public Adapter getAdapter(String adapterId, boolean forceUpdate) {
		for (Adapter a : getAdapters()) {
			if (a.getId().equals(adapterId)) {
				refreshAdapter(a, forceUpdate);
				return a;
			}
		}
		
		return null;
	}

	/**
	 * Registers new adapter to server.
	 * 
	 * @param id
	 * @return true on success, false otherwise
	 */
	public boolean registerAdapter(String id) {
		if (mDemoMode)
			return false;

		ActualUser acUser = ActualUser.getActualUser();
		try {
			return mNetwork.signUp(acUser.getEmail(), id, Integer.parseInt(acUser.getSessionId()));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * Unregisters adapter from server.
	 * 
	 * @param id
	 * @return true on success, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean unregisterAdapter(String id) {
		throw new NotImplementedException();
	}


	/** Location methods ****************************************************/
	
	/**
	 * Return all locations from all adapters.
	 * 
	 * @return List of LocationListing
	 */
	public List<LocationListing> getLocations() {
		List<LocationListing> listings = new ArrayList<LocationListing>();
		
		for (Adapter adapter : getAdapters()) {
			for (Location location : adapter.getLocations()) {
				listings.add(new LocationListing(location.getId(), location));
			}
		}
		
		return listings;
	}
	
	/**
	 * Return location by ID.
	 * 
	 * @param id
	 * @return
	 * @throws NotImplementedException
	 */
	public LocationListing getLocation(String id) {
		throw new NotImplementedException();
	}
	
	/**
	 * Add new location to server.
	 * 
	 * @param location
	 * @return
	 * @throws NotImplementedException
	 */
	public boolean addLocation(LocationListing location) {
		throw new NotImplementedException();
	}
	
	/**
	 * Deletes location from server.
	 * 
	 * @param location
	 * @return
	 * @throws NotImplementedException
	 */
	public boolean deleteLocation(LocationListing location) {
		throw new NotImplementedException();
	}
	
	/**
	 * Save changes of location to server.
	 * 
	 * @param location
	 * @return
	 * @throws NotImplementedException
	 */
	public boolean saveLocation(LocationListing location) {
		throw new NotImplementedException();
	}
	
	/**
	 * Rename location from all adapters.
	 * 
	 * @param location
	 * @param newName
	 * @return true always
	 */
	public boolean renameLocation(String name, String newName) {
		// TODO: Use rather saveLocation() method
	
		for (Adapter adapter : getAdapters()) {
			for (BaseDevice device : adapter.getDevicesByLocation(name)) {
				Location location = device.getLocation();
				location.setId(newName);
				location.setName(newName);
				
				device.setLocation(location);
				// TODO: Save to server (somehow effectively)
			}
		}
		
		return true;
	}

	
	/** Devices methods *****************************************************/
	
	/**
	 * Return device by ID from all adapters.
	 * 
	 * @param id
	 * @return device or null if no device is found
	 */
	public BaseDevice getDevice(String id) {
		BaseDevice device = null;
		
		for (Adapter adapter : getAdapters()) {
			device = adapter.getDeviceById(id);
			if (device != null)
				break;
		}
		
		if (device != null && device.needsUpdate()) {
			updateDevice(device);
		}
		
		return device;
	}
	
	/**
	 * Marks device ad hidden on server.
	 * 
	 * @param device
	 * @return true on success, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean hideDevice(BaseDevice device) {
		// TODO: replace this with saveDevice method?
		throw new NotImplementedException();
	}
	
	/**
	 * Marks device as visible on server.
	 * 
	 * @param device
	 * @return true on success, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean unhideDevice(BaseDevice device) {
		// TODO: replace this with saveDevice method?
		throw new NotImplementedException();
	}

	/**
	 * Return list of all uninitialized devices from all adapters.
	 * 
	 * @return List of devices (or empty list)
	 */
	public List<BaseDevice> getUninitializedDevices() {
		List<BaseDevice> list = new ArrayList<BaseDevice>();
		
		for (Adapter adapter : getAdapters()) {
			list.addAll(adapter.getUninitializedDevices());
		}
		
		return list;
	}
	
	/**
	 * Return list of all devices by location from all adapters.
	 * 
	 * @param location
	 * @return List of devices (or empty list)
	 */
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
		if (mDemoMode) {
			device.setInitialized(true);
			refreshDevice(device);
			return true;
		}
		
		ArrayList<BaseDevice> devices = new ArrayList<BaseDevice>();
		devices.add(device);
		
		boolean result = false;

		try {
			result = mNetwork.partial(devices);
			result = updateDevice(device);
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Save all settings of device to server.
	 * 
	 * @param device
	 * @return true on success, false otherwise
	 */
	public boolean saveDevice(BaseDevice device) {
		return saveDevice(device, SaveDevice.SAVE_ALL);
	}
	
	/**
	 * Return log for device.
	 * 
	 * @param device
	 * @return
	 * @throws NotImplementedException
	 */
	public DeviceLog getDeviceLog(BaseDevice device) {
		throw new NotImplementedException();
	}

	
	/** User methods ********************************************************/

	/**
	 * Get user by ID from adapter.
	 * 
	 * @param adapterId
	 * @param userId
	 * @return
	 * @throws NotImplementedException
	 */
	public User getUser(String adapterId, String userId) {
		throw new NotImplementedException();
	}

	/**
	 * Add user to adapter.
	 * 
	 * @param adapterId
	 * @param user
	 * @return
	 * @throws NotImplementedException
	 */
	public boolean addUser(String adapterId, User user) {
		throw new NotImplementedException();
	}
	
	/**
	 * Delete user from adapter.
	 * 
	 * @param adapterId
	 * @param user
	 * @return
	 * @throws NotImplementedException
	 */
	public boolean deleteUser(String adapterId, User user) {
		throw new NotImplementedException();
	}
	
	/**
	 * Save user settings to adapter.
	 * 
	 * @param adapterId
	 * @param user
	 * @return
	 * @throws NotImplementedException
	 */
	public boolean saveUser(String adapterId, User user) {
		throw new NotImplementedException();
	}
	
	
	/** Custom lists methods ************************************************/
	
	/**
	 * Return list of all custom lists.
	 * 
	 * @return List with listings or empty list
	 * @throws NotImplementedException
	 */
	public List<FavoritesListing> getCustomLists() {
		if (mHousehold.favoritesListings == null) {
			// TODO: load favorites from network
			throw new NotImplementedException();
		}
		
		return mHousehold.favoritesListings;
	}
	
	/**
	 * Return custom list by ID.
	 * 
	 * @param id
	 * @return listing or null if not found
	 * @throws NotImplementedException
	 */
	public FavoritesListing getCustomList(String id) {
		for (FavoritesListing l : getCustomLists()) {
			if (l.getId().equals(id))
				return l;
		}
		
		return null;
	}

	/**
	 * Add new custom list to server.
	 * 
	 * @param list
	 * @return true on success, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean addCustomList(FavoritesListing list) {
		throw new NotImplementedException();
	}
	
	/**
	 * Delete custom list from server.
	 * 
	 * @param list
	 * @return true on success, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean deleteCustomList(FavoritesListing list) {
		throw new NotImplementedException();
	}
	
	/**
	 * Save custom list settings to server.
	 * 
	 * @param list
	 * @return true on success, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean saveCustomList(FavoritesListing list) {
		throw new NotImplementedException();
	}

	public void ignoreUninitialized(List<BaseDevice> devices) {
		for (Adapter adapter : getAdapters()) {
			adapter.ignoreUninitialized(devices);
		}
	}

	public void unignoreUninitialized() {
		for (Adapter adapter : getAdapters()) {
			adapter.unignoreUninitialized();
		}
	}

}