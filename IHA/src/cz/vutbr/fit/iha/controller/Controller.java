package cz.vutbr.fit.iha.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.Component;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.StateDevice;
import cz.vutbr.fit.iha.adapter.device.SwitchDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.household.DemoHousehold;
import cz.vutbr.fit.iha.household.Household;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.network.GetGoogleAuth;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.network.exception.FalseException;
import cz.vutbr.fit.iha.network.exception.NetworkException;
import cz.vutbr.fit.iha.persistence.Persistence;

/**
 * Core of application (used as singleton), provides methods and access to all data and household.
 *
 * @author Robyer
 */
public final class Controller {

	public static final String TAG = Controller.class.getSimpleName();
	
	/** This singleton instance. */
	private static Controller mController;
	
	/** Application context */
	private final Context mContext;
	
	/** Persistence service for caching purposes */
	private final Persistence mPersistence;
	
	/** Network service for communication with server */
	private final Network mNetwork;
	
	/** Household object holds logged in user and all adapters and lists which belongs to him */
	private final Household mHousehold;
	
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

		mHousehold = mDemoMode ? new DemoHousehold(mContext) : new Household();
		mPersistence = new Persistence(mContext);
		mNetwork = new Network(mContext, mHousehold.user);
	}
	
	public static synchronized void setDemoMode(Context context, boolean demoMode) {
		if (mDemoMode == demoMode)
			return;
		
		mDemoMode = demoMode;
		mController = new Controller(context);
	}
	
	public static boolean isDemoMode() {
		return mDemoMode;
	}
	
	
	/** Persistence methods *************************************************/

	public String getLastEmail() {
		return mPersistence.loadLastEmail();
	}

	public SharedPreferences getUserSettings() throws IllegalStateException {
		String userId = mHousehold.user.getId();
		if (userId.isEmpty())
			throw new IllegalStateException("ActualUser is not initialized (logged in) yet");
		
		return mPersistence.getSettings(userId);
	}
	
	
	/** Communication methods ***********************************************/
	
	/**
	 * Login user by his email (authenticate on server).
	 * 
	 * @param email
	 * @return true on success, false otherwise
	 * @throws NetworkException
	 */
	public boolean login(String email) throws NetworkException {
		if (mDemoMode) {
			// TODO: remember and login automatically to demo next time?
			// mPersistence.saveLastEmail(...);
			mPersistence.initializeDefaultSettings(email);
			return true;
		}
		
		// TODO: catch and throw proper exception
		// FIXME: after some time there should be picture in ActualUser object, should save to mPersistence
		try {
			if (mNetwork.signIn(email)) {
				mPersistence.saveLastEmail(email);
				mPersistence.initializeDefaultSettings(email);
				return true;
			}
		} catch(FalseException e) {
			//FIXME: ROB, do this how you want, this is working code :)
			switch(e.getDetail().getErrCode()){
				case 0:
					break;
				case 1: // bad token or email
					try {
						//TODO: do this otherway
						GetGoogleAuth ggAuth = GetGoogleAuth.getGetGoogleAuth();
						
						ggAuth.invalidateToken();
						ggAuth.doInForeground((ggAuth.getPictureIMG() == null)? true:false);
						
						//this happen only on first signing (or when someone delete grants on google, or token is old)
						while(GetGoogleAuth.getGetGoogleAuth().getPictureIMG() == null);
						
						return login(email);
						
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				default:
					break;
			}
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
		mHousehold.user.logout();
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
		return mHousehold.user.isLoggedIn();
	}
	
	public boolean isInternetAvailable(){
		return mNetwork.isAvailable();
	}
	
	/** Adapter methods *****************************************************/

	/**
	 * Refreshes adapter data - loads devices and locations
	 * @param adapter
	 * @param forceUpdate if you want to refresh adapter even if it's perhaps not needed
	 * @return
	 */
	private boolean refreshAdapter(Adapter adapter, boolean forceUpdate) {
		if (mDemoMode) {
			return true;
		}
		
		Time that = new Time();
		that.setToNow();
		that.set(that.toMillis(true) - 15*60*1000); // 15 minutes interval between updates
		
		// Update only when needed
		if (!forceUpdate && !adapter.lastUpdate.before(that))
			return false;

		Log.i(TAG, String.format("Adapter (%s) update needed (%s)", adapter.getName(), forceUpdate ? "force" : "time elapsed"));

		Adapter newAdapter = new Adapter();
		List<Location> newLocations = null;
		int newUtcOffset = 0;
		
		try {
			newAdapter.setDevices(mNetwork.init(adapter.getId()));
			newLocations = mNetwork.getLocations(adapter.getId());
			newUtcOffset = mNetwork.getTimeZone(adapter.getId());
		} catch (NetworkException e) {
			e.printStackTrace();
			return false;
		}
		
		// Update adapter with new data
		adapter.setLocations(newLocations);
		adapter.setUtcOffset(newUtcOffset);

		adapter.setDevices(newAdapter.getDevices());
		adapter.setId(newAdapter.getId());
		adapter.setName(newAdapter.getName());
		adapter.setRole(newAdapter.getRole());
		
		adapter.lastUpdate.setToNow();
		
		return true;
	}
	
	/**
	 * Calling this will reload all adapters from server in next call of {@link #getAdapters()} or {@link #getActiveAdapter()}
	 */
	public void reloadAdapters() {
		mReloadAdapters = true;
	}
	
	/**
	 * Refreshes device in listings (e.g., in uninitialized devices)
	 * @param device
	 */
	public void refreshDevice(final BaseDevice device) {
		Adapter adapter = getActiveAdapter();
		if (adapter != null) {
			adapter.refreshDevice(device);
		}
	}
	
	public boolean updateDevice(BaseDevice device) {
		if (mDemoMode) {
			// In demo mode update device with random value 
			if (device instanceof SwitchDevice) {
				((SwitchDevice)device).setActive(new Random().nextBoolean());
			} else if (device instanceof StateDevice) {
				((StateDevice)device).setActive(new Random().nextBoolean());
			} else {
				int i = new Random().nextInt(100);
				device.setValue(i);
			}
			return true;
		}
		
		Adapter adapter = getAdapterByDevice(device);
		if (adapter == null)
			return false;
		
		List<BaseDevice> devices = new ArrayList<BaseDevice>();
		devices.add(device);

		try {
			devices = mNetwork.getDevices(adapter.getId(), devices);
			if (devices == null || devices.size() != 1)
				return false;
			
			BaseDevice newDevice = devices.get(0);
			device.setLocationId(newDevice.getLocationId());
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
		if (mDemoMode) {
			return mHousehold.adapters;
		}
		
		if (!isLoggedIn()) {
			return new ArrayList<Adapter>();
		}
		
		// TODO: refactor this method, make household's adapters (and favoriteslisting, and user?) final etc.
		if (mHousehold.adapters == null || mReloadAdapters) { 
			try { 
				mHousehold.adapters = mNetwork.getAdapters();
				mReloadAdapters = false;
			} catch (NetworkException e) {
				// Network or another error, we must return correct object now, but adapters must be loaded later
				mHousehold.adapters = new ArrayList<Adapter>();
				mReloadAdapters = true;

				e.printStackTrace();
			}
			
		}

		// Refresh all adapters (load their devices and locations)
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
	 * Return active adapter.
	 * 
	 * @return active adapter, or first adapter, or null if there are no adapters
	 */
	public synchronized Adapter getActiveAdapter() {
		if (mHousehold.activeAdapter == null || mReloadAdapters) {
			String lastId;
			if (mReloadAdapters && mHousehold.activeAdapter != null) {
				lastId = mHousehold.activeAdapter.getId();
			} else {
				lastId = mPersistence.loadActiveAdapter(mHousehold.user.getId());
			}
			
			for (Adapter a : getAdapters()) {
				if (lastId.isEmpty() || a.getId().equals(lastId)) {
					mHousehold.activeAdapter = a;
					break;
				}
			}
			
			if (mHousehold.activeAdapter == null && mHousehold.adapters != null && !mHousehold.adapters.isEmpty()) {
				mHousehold.activeAdapter = mHousehold.adapters.get(0);
			}
			
			if (mHousehold.activeAdapter != null)
				mPersistence.saveActiveAdapter(mHousehold.user.getId(), mHousehold.activeAdapter.getId());
		}
		
		return mHousehold.activeAdapter;
	}
	
	/**
	 * Sets active adapter.
	 * 
	 * @param id
	 * @return true on success, false if there is no adapter with this id
	 */
	public synchronized boolean setActiveAdapter(String id) {
		for (Adapter a : getAdapters()) {
			if (a.getId().equals(id)) {
				mHousehold.activeAdapter = a;
				refreshAdapter(a, true);
				Log.d(TAG, String.format("Set active adapter to '%s'", a.getName()));
				mPersistence.saveActiveAdapter(mHousehold.user.getId(), mHousehold.activeAdapter.getId());
				return true;
			}
		}

		Log.d(TAG, String.format("Can't set active adapter to '%s'", id));
		return false;
	}

	/**
	 * Return Adapter which this device belongs to.
	 * 
	 * @param device
	 * @return Adapter if found, null otherwise
	 */
	public Adapter getAdapterByDevice(BaseDevice device) {
		for (Adapter a : getAdapters()) {
			if (a.getDeviceById(device.getId()) != null)
				return a; 
		}
		
		return null;
	}
	
	/**
	 * Registers new adapter to server.
	 * 
	 * @param id
	 * @return true on success, false otherwise
	 */
	//FIXME: this register user not adapter
	public boolean registerAdapter(String id, String adapterName) {
		if (mDemoMode) {
			return false;
		}

		boolean result = false;
		
		try {
			if (mNetwork.addAdapter(id, adapterName)) {
				reloadAdapters(); // TODO: reload (or just add this adapter) only adapters list (without reloading devices)
				setActiveAdapter(id); // FIXME : kurvaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
				result = true;
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	//TODO: review this
	public boolean registerUser(String id){
		if (mDemoMode) {
			return false;
		}

		boolean result = false;
		
		try {
			result = mNetwork.signUp(mHousehold.user.getEmail());
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	/**
	 * FIXME: debug implementation
	 * Unregisters adapter from server.
	 * 
	 * @param id
	 * @return true on success, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean unregisterAdapter(String id) throws NotImplementedException {
		if (mDemoMode) {
			return false;
		}

		//TODO: this is debug implementation

		ArrayList<String> user = new ArrayList<String>();
		user.add(mHousehold.user.getEmail());

		boolean result = false;
		
		try {
			if (mNetwork.deleteConnectionAccounts(id, user)) {
				if(mHousehold.activeAdapter != null && mHousehold.activeAdapter.getId().equals(id))
					mHousehold.activeAdapter = null;
				reloadAdapters(); // TODO: reload (or just add this adapter) only adapters list (without reloading devices)
//				setActiveAdapter(id);
				result = true;
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		return result;
	}


	/** Location methods ****************************************************/
	
	/**
	 * Return location from active adapter by id.
	 * 
	 * @param id
	 * @return Location if found, null otherwise.
	 */
	public Location getLocation(String id) {
		// FIXME: should this be removed when there will be switching activeAdapter somehow, because one should call getLocation on adapter object?

		// TODO: or use getLocations() method as base?
		Adapter adapter = getActiveAdapter();
		if (adapter != null) {
			return adapter.getLocation(id);
		}
		
		return null;
	}
	
	/**
	 * Return location object that belongs to device.
	 * @param device
	 * @return Location if found, null otherwise.
	 */
	public Location getLocationByDevice(BaseDevice device) {
		Adapter adapter = getAdapterByDevice(device);
		if (adapter == null) {
			return null;
		}
		
		return adapter.getLocation(device.getLocationId());
	}
	
	/**
	 * Deletes location from server.
	 * 
	 * @param location
	 * @return
	 */
	public boolean deleteLocation(Location location) {
		Adapter adapter = getActiveAdapter();
		if (adapter == null) {
			return false;
		}

		boolean deleted = false;
		try {
			if (mDemoMode) {
				deleted = true;
			} else {	
				deleted = mNetwork.deleteLocation(adapter.getId(), location);
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		if (!deleted) {
			return false;
		}

		// Location was deleted on server, remove it from adapter too		
		return adapter.deleteLocation(location.getId());
	}
	
	/**
	 * Save new or changed location to server.
	 * 
	 * @param location
	 * @return new location object or null on error
	 */
	public Location saveLocation(Location location) {
		// TODO: separate it to 2 methods? (createLocation and saveLocation)
		Adapter adapter = getActiveAdapter();
		if (adapter == null) {
			return null;
		}

		boolean saved = false;
		boolean adding = location.getId().equals(Location.NEW_LOCATION_ID);
		try {
			if (adding) {
				if (mDemoMode) {
					location.setId(adapter.getUnusedLocationId());
					saved = true;
				} else {
					location = mNetwork.createLocation(adapter.getId(), location);
					saved = (location != null);
				}
			} else {
				if (mDemoMode) {
					saved = true;
				} else {
					List<Location> locations = new ArrayList<Location>();
					locations.add(location);
					saved = mNetwork.updateLocations(adapter.getId(), locations);
				}
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		
		if (!saved) {
			return null;
		}

		// Location was saved on server, save it to adapter too
		if (adding) {
			return adapter.addLocation(location) ? location : null;
		} else {
			return adapter.updateLocation(location) ? location : null;
		}
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
	public boolean hideDevice(BaseDevice device) throws NotImplementedException {
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
	public boolean unhideDevice(BaseDevice device) throws NotImplementedException {
		// TODO: replace this with saveDevice method?
		throw new NotImplementedException();
	}

	/**
	 * Return list of all uninitialized devices from active adapter.
	 * 
	 * @return List of devices (or empty list)
	 */
	public List<BaseDevice> getUninitializedDevices() {
		List<BaseDevice> list = new ArrayList<BaseDevice>();
		
		Adapter adapter = getActiveAdapter();
		if (adapter != null) {
			list.addAll(adapter.getUninitializedDevices());
		}
		
		return list;
	}
	
	/**
	 * Return list of all devices by location from active adapter.
	 * 
	 * @param location
	 * @return List of devices (or empty list)
	 */
	public List<BaseDevice> getDevicesByLocation(String locationId, boolean forceUpdate) {
		List<BaseDevice> list = new ArrayList<BaseDevice>();
		
		Adapter adapter = getActiveAdapter();
		if (adapter != null) {
			refreshAdapter(adapter, forceUpdate); // TODO: update only devices in this location? or no?
			list.addAll(adapter.getDevicesByLocation(locationId));
		}
		
		return list;
	}
	
	public List<BaseDevice> getDevicesByAdapter(String adapterId) {
		return getAdapter(adapterId, false).getDevices();
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
		
		List<BaseDevice> devices = new ArrayList<BaseDevice>();
		devices.add(device);
		
		boolean result = false;

		try {
			Adapter adapter = getAdapterByDevice(device);
			Log.d(TAG, "Adapter ID: "+adapter.getId()+ " device:"+device.getAddress());
			if (adapter != null) {
				result = mNetwork.setDevices(adapter.getId(), devices);
				result = updateDevice(device);
			}
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
	 */
	public DeviceLog getDeviceLog(BaseDevice device, String from, String to, DataType type, DataInterval interval) {
		// FIXME: rewrite this method even better - demo mode, caching, etc.
		DeviceLog log = new DeviceLog(DataType.AVERAGE, DataInterval.RAW);
		
		if (mDemoMode) {
			return log;
		}
		
		try {
			Adapter adapter = getAdapterByDevice(device);
			if (adapter != null) {
				log = mNetwork.getLog(adapter.getId(), device, from, to, type, interval);
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}

		return log;
	}
	
	/**
	 * Send pair request 
	 * @param stringID
	 * @return result
	 */
	public boolean sendPairRequest(String adapterID) {
		if (mDemoMode) {
			return false;
		}
		
		boolean result = false;
		
		try {
			result = mNetwork.prepareAdapterToListenNewSensors(adapterID);
		} catch (NetworkException e) {
			e.printStackTrace();
		}

		return result;
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
	public User getUser(String adapterId, String userId) throws NotImplementedException {
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
	public boolean addUser(String adapterId, User user) throws NotImplementedException {
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
	public boolean deleteUser(String adapterId, User user) throws NotImplementedException {
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
	public boolean saveUser(String adapterId, User user) throws NotImplementedException {
		throw new NotImplementedException();
	}
	

	public void ignoreUninitialized(List<BaseDevice> devices) {
		Adapter adapter = getActiveAdapter();
		if (adapter != null)
			adapter.ignoreUninitialized(devices);
	}

	public void unignoreUninitialized() {
		Adapter adapter = getActiveAdapter();
		if (adapter != null)
			adapter.unignoreUninitialized();
	}

	public ActualUser getActualUser() {
		return mHousehold.user;
	}
	
	public List<Component> getComponentsByLocation(String locationId, boolean forceUpdate) {
		List<Component> list = new ArrayList<Component>();
		
		Adapter adapter = getActiveAdapter();
		if (adapter != null) {
			refreshAdapter(adapter, forceUpdate); // TODO: update only devices in this location? or no?
			list.addAll(adapter.getComponentsByLocation(locationId).values());
		}
		
		return list;
	}

}