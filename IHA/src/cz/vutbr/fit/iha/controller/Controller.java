package cz.vutbr.fit.iha.controller;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.activity.LoginActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.DeviceType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.household.DemoHousehold;
import cz.vutbr.fit.iha.household.Household;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.household.User.Gender;
import cz.vutbr.fit.iha.network.GoogleAuth;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.network.exception.FalseException;
import cz.vutbr.fit.iha.network.exception.NetworkException;
import cz.vutbr.fit.iha.persistence.Persistence;
import cz.vutbr.fit.iha.util.Utils;

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

	/**
	 * Return singleton instance of this Controller. This is thread-safe.
	 * 
	 * @param context
	 *            This must be the global application context.
	 * @return singleton instance of controller
	 */
	public static Controller getInstance(Context context) {
		if (mController == null) {
			synchronized (Controller.class) {
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
	 * @param context
	 *            This must be the global application context.
	 */
	private Controller(Context context) {
		mContext = context;

		mNetwork = new Network(mContext, this, Utils.isDebugVersion(context));
		mPersistence = new Persistence(mContext);
		mHousehold = mDemoMode ? new DemoHousehold(mContext, mNetwork) : new Household(mContext, mNetwork);
		mNetwork.setUser(mHousehold.user);
	}

	/**
	 * @param context
	 *            This must be the global Application context.
	 * @param demoMode
	 */
	public static synchronized void setDemoMode(Context context, boolean demoMode) {
		if (mDemoMode == demoMode)
			return;

		mDemoMode = demoMode;
		mController = new Controller(context);
		
		if (demoMode) {
			// Initialize default settings for demo mode, because in demo mode we don't call login()
			mController.mPersistence.initializeDefaultSettings(mController.mHousehold.user.getEmail());
		}
	}

	public static boolean isDemoMode() {
		return mDemoMode;
	}

	/** Persistence methods *************************************************/

	public String getLastEmail() {
		return mPersistence.loadLastEmail();
	}

	/**
	 * Get SharedPreferences for actually logged in user
	 *  
	 * @return null if user is not logged in
	 */
	public SharedPreferences getUserSettings() {
		String userEmail = mHousehold.user.getEmail();
		if (userEmail == null || userEmail.isEmpty()) {
			return null;
		}

		return mPersistence.getSettings(userEmail);
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
			mHousehold.user.setName("John Doe");
			mHousehold.user.setEmail(email);
			mHousehold.user.setGender(Gender.Male);
			mHousehold.user.setSessionId("123456789");

			mPersistence.initializeDefaultSettings(email);
			return true;
		}

		// TODO: catch and throw proper exception
		// FIXME: after some time there should be picture in ActualUser object, should save to mPersistence
		try {
			if (mNetwork.signIn(email, "")) { // FIXME: gcmid
				mPersistence.saveLastEmail(email);
				mPersistence.initializeDefaultSettings(email);
				return true;
			}
		} catch (FalseException e) {
			// FIXME: ROB, do this how you want, this is working code :)
			switch (e.getDetail().getErrCode()) {
			case 0:
				break;
			case 1: // bad token or email
				try {
					// TODO: do this otherway
					// GoogleAuth ggAuth = GoogleAuth.getGoogleAuth();
					//
					// ggAuth.invalidateToken();
					// ggAuth.doInForeground((ggAuth.getPictureIMG() == null)? true:false);

					mNetwork.startGoogleAuth(true, getActualUser().isPictureDefault() ? true : false);

					// this happen only on first signing (or when someone delete grants on google, or token is old)
					// while(GoogleAuth.getGoogleAuth().getPictureIMG() == null);
					while (getActualUser().isPictureDefault())
						; // FIXME: not sure with this, need to check (first sing in)

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

	public boolean isInternetAvailable() {
		return mNetwork.isAvailable();
	}

	/** Reloading data methods **********************************************/

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadAdapters(boolean forceReload) {
		if (mDemoMode || !isLoggedIn()) {
			return false;
		}

		return mHousehold.adaptersModel.reloadAdapters(forceReload);
	}

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadLocations(String adapterId, boolean forceReload) {
		if (mDemoMode || !isLoggedIn()) {
			return false;
		}

		return mHousehold.locationsModel.reloadLocationsByAdapter(adapterId, forceReload);
	}

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadFacilitiesByAdapter(String adapterId, boolean forceReload) {
		if (mDemoMode || !isLoggedIn()) {
			return false;
		}

		return mHousehold.facilitiesModel.reloadFacilitiesByAdapter(adapterId, forceReload);
	}

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadUninitializedFacilitiesByAdapter(String adapterId, boolean forceReload) {
		if (mDemoMode || !isLoggedIn()) {
			return false;
		}

		return mHousehold.uninitializedFacilitiesModel.reloadUninitializedFacilitiesByAdapter(adapterId, forceReload);
	}

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param facility
	 * @return
	 */
	public boolean updateFacility(Facility facility) {
		if (mDemoMode) {
			// In demo mode update facility devices with random values
			/*for (BaseDevice device : facility.getDevices()) {
				if (device instanceof SwitchDevice) {
					((OnOffValue) device.getValue()).setActive(new Random().nextBoolean());
				} else if (device instanceof StateDevice) {
					((OpenClosedValue) device.getValue()).setActive(new Random().nextBoolean());
				} else {
					int i = new Random().nextInt(100);
					device.getValue().setValue(i);
				}
			}*/
			return true;
		}

		return mHousehold.facilitiesModel.refreshFacility(facility);
	}

	/**
	 * Return all adapters that this logged in user has access to.
	 * 
	 * @return List of adapters
	 */
	public List<Adapter> getAdapters() {
		return mHousehold.adaptersModel.getAdapters();
	}

	/**
	 * Return adapter by his ID.
	 * 
	 * @param id
	 * @return Adapter if found, null otherwise
	 */
	public Adapter getAdapter(String id) {
		return mHousehold.adaptersModel.getAdapter(id);
	}

	/**
	 * Return active adapter.
	 * 
	 * @return active adapter, or first adapter, or null if there are no adapters
	 */
	public synchronized Adapter getActiveAdapter() {
		if (mHousehold.activeAdapter == null) {
			// UserSettings can be null when user is not logged in!
			SharedPreferences prefs = getUserSettings();
			
			String lastId = (prefs == null) ? "" : prefs.getString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, "");

			Map<String, Adapter> adapters = mHousehold.adaptersModel.getAdaptersMap();
			if (!lastId.isEmpty() && adapters.containsKey(lastId)) {
				mHousehold.activeAdapter = adapters.get(lastId);
			} else {
				for (Adapter adapter : adapters.values()) {
					mHousehold.activeAdapter = adapter;
					break;
				}
			}

			if (mHousehold.activeAdapter != null && prefs != null)
				prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, mHousehold.activeAdapter.getId()).commit();
		}

		return mHousehold.activeAdapter;
	}

	/**
	 * Sets active adapter and load all locations and facilities, if needed (or if forceReload = true)
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param id
	 * @param forceReload
	 * @return true on success, false if there is no adapter with this id
	 */
	public synchronized boolean setActiveAdapter(String id, boolean forceReload) {
		Map<String, Adapter> adapters = mHousehold.adaptersModel.getAdaptersMap();
		if (!adapters.containsKey(id)) {
			Log.d(TAG, String.format("Can't set active adapter to '%s'", id));
			return false;
		}

		Adapter adapter = adapters.get(id);
		mHousehold.activeAdapter = adapter;
		Log.d(TAG, String.format("Set active adapter to '%s'", adapter.getName()));
		
		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = getUserSettings();
		if (prefs != null) {
			prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, adapter.getId()).commit();
		}

		// Load locations and facilities, if needed
		reloadLocations(id, forceReload);
		reloadFacilitiesByAdapter(id, forceReload);

		return true;
	}

	/**
	 * Registers new adapter to server. Automatically reloads list of adapters, set this adapter as active and load all its sensors.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param id
	 * @return true on success, false otherwise
	 */
	public boolean registerAdapter(String id, String adapterName) {
		if (mDemoMode) {
			return false;
		}

		boolean result = false;

		try {
			if (mNetwork.addAdapter(id, adapterName)) {
				mHousehold.adaptersModel.reloadAdapters(true);
				setActiveAdapter(id, true);
				result = true;
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param email
	 * @return
	 */
	// TODO: review this
	public boolean registerUser(String email) {
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
	 * FIXME: debug implementation Unregisters adapter from server.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param id
	 * @return true on success, false otherwise
	 */
	public boolean unregisterAdapter(String id) throws NotImplementedException {
		if (mDemoMode) {
			return false;
		}

		// FIXME: This debug implementation unregisters actual user from adapter, not adapter itself

		boolean result = false;

		try {
			if (mNetwork.deleteAccount(id, mHousehold.user)) {
				if (mHousehold.activeAdapter != null && mHousehold.activeAdapter.getId().equals(id))
					mHousehold.activeAdapter = null;

				mHousehold.adaptersModel.reloadAdapters(true);
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
	public Location getLocation(String adapterId, String id) {
		return mHousehold.locationsModel.getLocation(adapterId, id);
	}

	/**
	 * Return list of locations from active adapter.
	 * 
	 * @return List of locations (or empty list)
	 */
	public List<Location> getLocations(String adapterId) {
		return mHousehold.locationsModel.getLocationsByAdapter(adapterId);
	}

	/**
	 * Deletes location from server.
	 * 
	 * This CAN'T be called on UI thread!
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

		// Location was deleted on server, remove it from adapter too
		return deleted && mHousehold.locationsModel.deleteLocation(adapter.getId(), location.getId());
	}

	/**
	 * Save changed location to server.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param location
	 * @return new location object or null on error
	 */
	public boolean saveLocation(Location location) {
		Adapter adapter = getActiveAdapter();
		if (adapter == null) {
			return false;
		}

		boolean saved = false;
		try {
			if (mDemoMode) {
				saved = true;
			} else {
				saved = mNetwork.updateLocation(adapter.getId(), location);
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}

		// Location was updated on server, update it to adapter too
		return saved && mHousehold.locationsModel.updateLocation(adapter.getId(), location);
	}

	/**
	 * Create and add new location to server.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param location
	 * @return new location object or null on error
	 */
	public Location addLocation(Location location) {
		Adapter adapter = getActiveAdapter();
		if (adapter == null) {
			return null;
		}

		try {
			if (mDemoMode) {
				location.setId(mHousehold.locationsModel.getUnusedLocationId(adapter.getId()));
			} else {
				location = mNetwork.createLocation(adapter.getId(), location);
			}
		} catch (NetworkException e) {
			e.printStackTrace();
			location = null;
		}

		// Location was saved on server, save it to adapter too
		return (location != null && mHousehold.locationsModel.addLocation(adapter.getId(), location)) ? location : null;
	}

	/** Facilities methods **************************************************/

	/**
	 * Return facility by ID.
	 * 
	 * @param id
	 * @return facility or null if no facility is found
	 */
	public Facility getFacility(String adapterId, String id) {
		return mHousehold.facilitiesModel.getFacility(adapterId, id);
	}

	public BaseDevice getDevice(String adapterId, String id) {
		String[] ids = id.split(BaseDevice.ID_SEPARATOR, 2);

		Facility facility = getFacility(adapterId, ids[0]);
		if (facility == null)
			return null;

		int iType = Integer.parseInt(ids[1]);
		DeviceType type = DeviceType.fromValue(iType);
		
		return facility.getDeviceByType(type);
	}

	/**
	 * Marks device as hidden on server.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param device
	 * @return true on success, false otherwise
	 */
	public boolean hideDevice(BaseDevice device) throws NotImplementedException {
		device.setVisibility(false);
		return saveDevice(device, EnumSet.of(SaveDevice.SAVE_VISIBILITY));
	}

	/**
	 * Marks device as visible on server.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param device
	 * @return true on success, false otherwise
	 */
	public boolean unhideDevice(BaseDevice device) throws NotImplementedException {
		device.setVisibility(true);
		return saveDevice(device, EnumSet.of(SaveDevice.SAVE_VISIBILITY));
	}

	/**
	 * Return list of all uninitialized facilities from adapter
	 * 
	 * @param adapterId
	 * @param withIgnored
	 * @return List of uninitialized facilities (or empty list)
	 */
	public List<Facility> getUninitializedFacilities(String adapterId, boolean withIgnored) {
		return mHousehold.uninitializedFacilitiesModel.getUninitializedFacilitiesByAdapter(adapterId, withIgnored);
	}

	/**
	 * Set all uninitialized facilities from adapter as ignored (won't be returned by calling getUninitializedFacilities)
	 * 
	 * @param adapterId
	 */
	public void ignoreUninitializedFacilities(String adapterId) {
		mHousehold.uninitializedFacilitiesModel.ignoreUninitalizedFacilities(adapterId);
	}

	/**
	 * Stop ignoring all ignored uninitialized facilities from adapter (will be returned by calling getUninitializedFacilities)
	 * 
	 * @param adapterId
	 */
	public void unignoreUninitialized(String adapterId) {
		mHousehold.uninitializedFacilitiesModel.unignoreUninitializedFacilities(adapterId);
	}

	/**
	 * Return list of all facilities by location from adapter
	 * 
	 * @param location
	 * @return List of facilities (or empty list)
	 */
	public List<Facility> getFacilitiesByLocation(String adapterId, String locationId) {
		return mHousehold.facilitiesModel.getFacilitiesByLocation(adapterId, locationId);
	}

	/**
	 * Return list of all facilities from adapter
	 * 
	 * @param adapterId
	 * @return List of facilities (or empty list)
	 */
	public List<Facility> getFacilitiesByAdapter(String adapterId) {
		return mHousehold.facilitiesModel.getFacilitiesByAdapter(adapterId);
	}

	/**
	 * Save specified settings of facility to server.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param facility
	 * @param what
	 *            type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveFacility(Facility facility, EnumSet<SaveDevice> what) {
		// FIXME: fix demoMode
		return mHousehold.facilitiesModel.saveFacility(facility, what);
	}

	/**
	 * Save specified settings of device to server.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param device
	 * @param what
	 *            type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveDevice(BaseDevice device, EnumSet<SaveDevice> what) {
		// FIXME: fix demoMode
		return mHousehold.facilitiesModel.saveDevice(device, what);
	}

	/**
	 * Return log for device.
	 * 
	 * This CAN'T be called on UI thread!
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
			Adapter adapter = getAdapter(device.getFacility().getAdapterId());
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
	 * 
	 * This CAN'T be called on UI thread!
	 * 
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

	/**
	 * TODO: this is NEW method initializing googleAuth in network
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param activity
	 * @param email
	 */
	public void initGoogle(LoginActivity activity, String email) {
		mNetwork.initGoogle(new GoogleAuth(activity, email));
	}

	/**
	 * TODO: this is NEW method for start google communication
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param blocking
	 *            -> look at network
	 * @param fetchPhoto
	 *            -> look at network
	 * @return -> look at network
	 */
	public boolean startGoogle(boolean blocking, boolean fetchPhoto) {
		return mNetwork.startGoogleAuth(blocking, fetchPhoto);
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	public String getGCMRegistrationId() {
		String registrationId = mPersistence.loadGCMRegistrationId();
		if (registrationId.isEmpty()) {
			Log.i(TAG, "GCM: Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = mPersistence.loadLastApplicationVersion();
		int currentVersion = Utils.getAppVersion(mContext);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "GCM: App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 * 
	 * @param regId
	 *            registration ID
	 */
	public void setGCMRegistrationId(String regId) {
		int appVersion = Utils.getAppVersion(mContext);
		Log.i(TAG, "Saving regId on app version " + appVersion);

		mPersistence.saveGCMRegistrationId(regId);
		mPersistence.saveLastApplicationVersion(appVersion);
	}

	public ActualUser getActualUser() {
		return mHousehold.user;
	}

}