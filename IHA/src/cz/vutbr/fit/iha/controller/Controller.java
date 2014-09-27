package cz.vutbr.fit.iha.controller;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import cz.vutbr.fit.iha.activity.LoginActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.device.StateDevice;
import cz.vutbr.fit.iha.adapter.device.SwitchDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.gcm.GcmHelper;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.household.DemoHousehold;
import cz.vutbr.fit.iha.household.Household;
import cz.vutbr.fit.iha.household.User;
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

	/** When set to true (by calling {@link #reloadAdapters()}), it will reload all adapters from server in next call of {@link #getAdapters()} */
	private boolean mReloadAdapters = true;

	/**
	 * Return singleton instance of this Controller. This is thread-safe.
	 * 
	 * @param global
	 *            application context
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
	 * @param global
	 *            application context
	 */
	private Controller(Context context) {
		mContext = context;

		mHousehold = mDemoMode ? new DemoHousehold(mContext) : new Household();
		mPersistence = new Persistence(mContext);
		mNetwork = new Network(mContext, mHousehold.user, isDebugVersion());
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

	/**
	 * Check if this is debug version of application
	 * 
	 * @return true if version in Manifest contains "debug", false otherwise
	 */
	private boolean isDebugVersion() {
		try {
			String version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
			return version.contains("debug");
		} catch (NameNotFoundException e) {
		}

		return false;
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
			if (mNetwork.signIn(email, GcmHelper.getGCMRegistrationId(mContext))) { //FIXME: gcmid
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

	/** Adapter methods *****************************************************/

	/**
	 * Refreshes adapter data - loads facilities and locations
	 * 
	 * @param adapter
	 * @param forceUpdate
	 *            if you want to refresh adapter even if it's perhaps not needed
	 * @return
	 */
	private boolean refreshAdapter(Adapter adapter, boolean forceUpdate) {
		if (mDemoMode) {
			return true;
		}

		// Update only when needed
		if (!forceUpdate && !Utils.isExpired(adapter.lastUpdate, 15 * 60))
			return false;

		Log.i(TAG, String.format("Adapter (%s) update needed (%s)", adapter.getName(), forceUpdate ? "force" : "time elapsed"));

		boolean result = false;

		try {
			// Update adapter with new data
			adapter.setLocations(mNetwork.getLocations(adapter.getId()));
			adapter.setUtcOffset(mNetwork.getTimeZone(adapter.getId()));
			adapter.setFacilities(mNetwork.init(adapter.getId()));

			adapter.lastUpdate.setToNow();
			result = true;
		} catch (NetworkException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Calling this will reload all adapters from server in next call of {@link #getAdapters()} or {@link #getActiveAdapter()}
	 */
	public void reloadAdapters() {
		mReloadAdapters = true;
	}

	/**
	 * Refreshes facility in listings (e.g., in uninitialized facilities)
	 * 
	 * @param facility
	 */
	public void refreshFacility(final Facility facility) {
		Adapter adapter = getActiveAdapter();
		if (adapter != null) {
			adapter.refreshFacility(facility);
		}
	}

	public boolean updateFacility(Facility facility) {
		if (mDemoMode) {
			// In demo mode update facility devices with random values
			for (BaseDevice device : facility.getDevices()) {
				if (device instanceof SwitchDevice) {
					((SwitchDevice) device).setActive(new Random().nextBoolean());
				} else if (device instanceof StateDevice) {
					((StateDevice) device).setActive(new Random().nextBoolean());
				} else {
					int i = new Random().nextInt(100);
					device.setValue(i);
				}
			}
			return true;
		}

		Adapter adapter = getAdapterByFacility(facility);
		if (adapter == null)
			return false;

		try {
			Facility newFacility = mNetwork.getFacility(adapter.getId(), facility);
			if (newFacility == null)
				return false;

			facility.replaceData(newFacility);
		} catch (NetworkException e) {
			e.printStackTrace();
			return false;
		}

		refreshFacility(facility);
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
				e.printStackTrace();
				// Network or another error, we must return correct object now, but adapters must be loaded later
				mHousehold.adapters = new ArrayList<Adapter>();
				mReloadAdapters = true;
			}

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

		// Refresh active adapter (load its locations and facilities)
		if (mHousehold.activeAdapter != null)
			refreshAdapter(mHousehold.activeAdapter, false);

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
	 * Return Adapter which this facility belongs to.
	 * 
	 * @param facility
	 * @return Adapter if found, null otherwise
	 */
	public Adapter getAdapterByFacility(Facility facility) {
		String adapterId = facility.getAdapterId();
		if (adapterId.length() > 0)
			return getAdapter(adapterId, false);

		// FIXME: remove when facilities will have correctly set adapterId
		for (Adapter a : getAdapters()) {
			if (a.getFacilityById(facility.getId()) != null)
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
	// FIXME: this register user not adapter
	public boolean registerAdapter(String id, String adapterName) {
		if (mDemoMode) {
			return false;
		}

		boolean result = false;

		try {
			if (mNetwork.addAdapter(id, adapterName)) {
				reloadAdapters(); // TODO: reload (or just add this adapter) only adapters list (without reloading facilities)
				setActiveAdapter(id); // FIXME : kurvaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
				result = true;
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}

		return result;
	}

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
	 * @param id
	 * @return true on success, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean unregisterAdapter(String id) throws NotImplementedException {
		if (mDemoMode) {
			return false;
		}

		// TODO: this is debug implementation

		ArrayList<String> user = new ArrayList<String>();
		user.add(mHousehold.user.getEmail());

		boolean result = false;

		try {
			if (mNetwork.deleteConnectionAccounts(id, user)) {
				if (mHousehold.activeAdapter != null && mHousehold.activeAdapter.getId().equals(id))
					mHousehold.activeAdapter = null;
				reloadAdapters(); // TODO: reload (or just add this adapter) only adapters list (without reloading facilities)
				// setActiveAdapter(id);
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
		Adapter adapter = getActiveAdapter();
		if (adapter == null)
			return null;

		return adapter.getLocation(id);
	}

	/**
	 * Return list of locations from active adapter.
	 * 
	 * @return List of locations (or empty list)
	 */
	public List<Location> getLocations() {
		Adapter adapter = getActiveAdapter();
		if (adapter == null)
			return new ArrayList<Location>();

		return adapter.getLocations();
	}

	/**
	 * Return location object that belongs to facility.
	 * 
	 * @param facility
	 * @return Location if found, null otherwise.
	 */
	public Location getLocationByFacility(Facility facility) {
		Adapter adapter = getAdapterByFacility(facility);
		if (adapter == null) {
			return null;
		}

		return adapter.getLocation(facility.getLocationId());
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

		// Location was deleted on server, remove it from adapter too
		return deleted && adapter.deleteLocation(location.getId());
	}

	/**
	 * Save changed location to server.
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
		return saved && adapter.updateLocation(location);
	}

	/**
	 * Create and add new location to server.
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
				location.setId(adapter.getUnusedLocationId());
			} else {
				location = mNetwork.createLocation(adapter.getId(), location);
			}
		} catch (NetworkException e) {
			e.printStackTrace();
			location = null;
		}

		// Location was saved on server, save it to adapter too
		return (location != null && adapter.addLocation(location)) ? location : null;
	}

	/** Facilities methods **************************************************/

	/**
	 * Return facility by ID from all adapters.
	 * 
	 * @param id
	 * @return facility or null if no facility is found
	 */
	public Facility getFacility(String id) {
		Facility facility = null;

		for (Adapter adapter : getAdapters()) {
			facility = adapter.getFacilityById(id);
			if (facility != null)
				break;
		}

		if (facility != null && Utils.isExpired(facility.lastUpdate, facility.getRefresh().getInterval())) {
			updateFacility(facility);
		}

		return facility;
	}

	public BaseDevice getDevice(String id) {
		String[] ids = id.split(BaseDevice.ID_SEPARATOR, 2);

		Facility facility = getFacility(ids[0]);
		if (facility == null)
			return null;

		return facility.getDeviceByType(Integer.parseInt(ids[1]));
	}

	/**
	 * Marks device as hidden on server.
	 * 
	 * @param device
	 * @return true on success, false otherwise
	 */
	public boolean hideDevice(BaseDevice device) throws NotImplementedException {
		device.getFacility().setVisibility(false);
		return saveDevice(device, EnumSet.of(SaveDevice.SAVE_VISIBILITY)); // TODO: this should be for facility, not device
	}

	/**
	 * Marks device as visible on server.
	 * 
	 * @param device
	 * @return true on success, false otherwise
	 */
	public boolean unhideDevice(BaseDevice device) throws NotImplementedException {
		device.getFacility().setVisibility(true);
		return saveDevice(device, EnumSet.of(SaveDevice.SAVE_VISIBILITY)); // TODO: this should be for facility, not device
	}

	/**
	 * Return list of all uninitialized facilities from active adapter.
	 * 
	 * @return List of facilities (or empty list)
	 */
	public List<Facility> getUninitializedFacilities() {
		List<Facility> list = new ArrayList<Facility>();

		Adapter adapter = getActiveAdapter();
		if (adapter != null) {
			list.addAll(adapter.getUninitializedFacilities());
		}

		return list;
	}

	/**
	 * Return list of all facilities by location from active adapter.
	 * 
	 * @param location
	 * @return List of facilities (or empty list)
	 */
	public List<Facility> getFacilitiesByLocation(String locationId, boolean forceUpdate) {
		List<Facility> list = new ArrayList<Facility>();

		Adapter adapter = getActiveAdapter();
		if (adapter != null) {
			refreshAdapter(adapter, forceUpdate); // TODO: update only facilities in this location? or no?
			list.addAll(adapter.getFacilitiesByLocation(locationId));
		}

		return list;
	}

	public List<Facility> getFacilitiesByAdapter(String adapterId) {
		return getAdapter(adapterId, false).getFacilities();
	}

	/**
	 * Save specified settings of device to server.
	 * 
	 * @param device
	 * @param what
	 *            type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveDevice(BaseDevice device, EnumSet<SaveDevice> what) {
		Facility facility = device.getFacility();

		if (mDemoMode) {
			facility.setInitialized(true);
			refreshFacility(facility);
			return true;
		}

		boolean result = false;

		try {
			Adapter adapter = getAdapterByFacility(facility);
			Log.d(TAG, String.format("Adapter ID: %s, device: %s", adapter.getId(), facility.getAddress()));
			if (adapter != null) {
				result = mNetwork.setDevice(adapter.getId(), device, what);
				result = updateFacility(facility);
			}
		} catch (NetworkException e) {
			e.printStackTrace();
		}

		return result;
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
			Adapter adapter = getAdapterByFacility(device.getFacility());
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
	 * @param activity
	 * @param email
	 */
	public void initGoogle(LoginActivity activity, String email) {
		mNetwork.initGoogle(new GoogleAuth(activity, email));
	}

	/**
	 * TODO: this is NEW method for start google communication
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

	public void ignoreUninitialized(List<Facility> facilities) {
		Adapter adapter = getActiveAdapter();
		if (adapter != null)
			adapter.ignoreUninitialized(facilities);
	}

	public void unignoreUninitialized() {
		Adapter adapter = getActiveAdapter();
		if (adapter != null)
			adapter.unignoreUninitialized();
	}

	public ActualUser getActualUser() {
		return mHousehold.user;
	}

}