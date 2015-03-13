package com.rehivetech.beeeon.controller;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.activity.LoginActivity;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Device.SaveDevice;
import com.rehivetech.beeeon.adapter.device.DeviceLog;
import com.rehivetech.beeeon.adapter.device.DeviceType;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.exception.NotImplementedException;
import com.rehivetech.beeeon.gcm.GcmHelper;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.Notification;
import com.rehivetech.beeeon.household.ActualUser;
import com.rehivetech.beeeon.household.Household;
import com.rehivetech.beeeon.household.User;
import com.rehivetech.beeeon.household.User.Role;
import com.rehivetech.beeeon.network.DemoNetwork;
import com.rehivetech.beeeon.network.GoogleAuthHelper;
import com.rehivetech.beeeon.network.GoogleAuthHelper.GoogleUserInfo;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.Network;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

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
	private final INetwork mNetwork;

	/** Household object holds logged in user and all adapters and lists which belongs to him */
	private final Household mHousehold;

	/** Switch for using demo mode (with example adapter, without server) */
	private static boolean mDemoMode = false;

	/** Weak map for holding registered notification receivers */
	private final WeakHashMap<INotificationReceiver, Boolean> mNotificationReceivers = new WeakHashMap<INotificationReceiver, Boolean>();

	private List<User> mRequestUsers;

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

		mNetwork = mDemoMode ? new DemoNetwork(context) : new Network(mContext, this, Utils.isDebugVersion(context));
		mPersistence = new Persistence(mContext);
		mHousehold = new Household(mContext, mNetwork);
	}
	
	/**
	 * Recreates the actual Controller object to use with different user or demo mode. 
	 * 
	 * @param context
	 *            This must be the global Application context.
	 * @param demoMode
	 */
	public static synchronized void setDemoMode(Context context, boolean demoMode) {
		// We always need to create a new Controller, due to account switch and first (not) loading of demo
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
	
	/**
	 * Get global SharedPreferences for whole application
	 */
	public SharedPreferences getGlobalSettings() {
		return mPersistence.getSettings(Persistence.GLOBAL);
	}

	/** Communication methods ***********************************************/

	/**
	 * Login user by his email (authenticate on server).
	 * 
	 * Currently support only GoogleAuth or demoMode
	 * 
	 * @param email
	 * @return true on success, false otherwise
	 * @throws AppException
	 */
	public boolean login(String email) throws AppException {
		return login(email, true);
	}

	/**
	 * @see {@link Controller#login(LoginActivity, String)}
	 */
	private boolean login(String email, boolean canTryAgain) throws AppException {
		ActualUser user = mHousehold.user;
		GoogleUserInfo googleUserInfo = null;
		String googleToken = "";

		try {
			// Load UID from previous session
			String UID = mPersistence.loadLastUID(email);
			Log.i(TAG, String.format("Loaded cached UID: %s", UID));
			mNetwork.setUID(UID);

			// Try to do test request with loaded UID to know if we're really logged in or not
			if (!UID.isEmpty()) {
				try {
					// FIXME: Use some better request
					mNetwork.getAdapters();
				} catch (AppException e) {
					ErrorCode errorCode = e.getErrorCode();
					if (errorCode instanceof NetworkError && errorCode == NetworkError.BAD_UID) {
						// This UID is invalid, load fresh one
						Log.w(TAG, "UID is invalid, we will load fresh one");
						UID = "";
					}
				}
			}

			// Load also cached user details
			mPersistence.loadUserDetails(email, user);

            // In demo mode load some init data from sdcard
            if (mNetwork instanceof DemoNetwork)
                ((DemoNetwork) mNetwork).initDemoData(user);

			if (UID.isEmpty() || user.isEmpty()) {
				// No previous session or user data, load fresh data from server
				if (mNetwork instanceof Network) {
					// Get Google token
					googleToken = GoogleAuthHelper.getToken(mContext, email);

					// Get user info from Google
					googleUserInfo = GoogleAuthHelper.fetchInfoFromProfileServer(googleToken);

					// Not loaded picture bitmap or user has new picture (avatarUrl is different)
					if ((!user.getPictureUrl().isEmpty() && user.getPicture() == null)
							|| !user.getPictureUrl().equals(googleUserInfo.pictureUrl)) {
						// No or changed picture, let's download it from server
						Bitmap picture = Utils.fetchImageFromUrl(googleUserInfo.pictureUrl);
						user.setPicture(picture);
					}

					user.setName(googleUserInfo.name);
					user.setEmail(email);
					user.setGender(googleUserInfo.gender);
					user.setPictureUrl(googleUserInfo.pictureUrl);
					user.setGoogleId(googleUserInfo.id);
				}

				// googleUserInfo must be initialized by code above
				if (!mNetwork.loadUID(googleUserInfo))
					return false;

				// Save our new UID
				Log.i(TAG, String.format("Loaded fresh UID: %s", mNetwork.getUID()));
				mPersistence.saveLastUID(email, mNetwork.getUID());
				// We have also fresh user detail, save them too
				mPersistence.saveUserDetails(email, user);
			}

			// Do we have session now? Then remember this user
			if (!mNetwork.getUID().isEmpty()) {
				mPersistence.initializeDefaultSettings(email);

				// Remember this email to use with auto login (but not in demoMode)
				if (!(mNetwork instanceof DemoNetwork)) {
					mPersistence.saveLastEmail(email);

					// Send GCM ID to server
					final String gcmId = mController.getGCMRegistrationId();
					if (gcmId.isEmpty()) {
						GcmHelper.registerGCMInBackground(mContext);
						Log.w(GcmHelper.TAG_GCM, "GCM ID is not accesible in persistant, creating new thread");
					} else {
						// send GCM ID to server
						Thread t = new Thread() {
							public void run() {
								try {
									mController.setGCMIdServer(gcmId);
								} catch (Exception e) {
									// do nothing
									Log.w(GcmHelper.TAG_GCM, "Login: Sending GCM ID to server failed: " + e.getLocalizedMessage());
								}
							}
						};
						t.start();
						mController.setGCMIdLocal(gcmId);
					}
				}

				return true;
			}
		} catch (AppException e) {
			// Process known and processible error codes (actually only google token error)
			ErrorCode errorCode = e.getErrorCode();
			if ((mNetwork instanceof Network) && (errorCode instanceof NetworkError)) {
				if (errorCode == NetworkError.NOT_VALID_USER || errorCode == NetworkError.GOOGLE_TOKEN) {
					// We have probably used incorrect Google token, invalidate it and try it again
					GoogleAuthHelper.invalidateToken(mContext, googleToken);

					// And try it again (if we haven't yet), hopefully we will have correct token then
					if (canTryAgain)
						return login(email, false);
				}
			}
			throw AppException.wrap(e);
		}
		return false;
	}

	/**
	 * Destroy user session in network and forget him as last logged in user.
	 */
	public void logout() {
		// TODO: Request to logout from server (discard actual communication UID)

		// Delete GCM ID on server side
		if (!(mNetwork instanceof DemoNetwork)) {
			final String email = getActualUser().getEmail();
			final String gcmId = getGCMRegistrationId();
			if (email != null && !gcmId.isEmpty()) {
				// delete GCM ID from server
				Thread t = new Thread() {
					public void run() {
						try {
							deleteGCM(email, gcmId);
						} catch (Exception e) {
							// do nothing
							Log.w(GcmHelper.TAG_GCM, "Logout: Delete GCM ID failed: " + e.getLocalizedMessage());
						}
					}
				};
				t.start();
			}
		}

		// Destroy session
		mNetwork.setUID("");

		// Delete session from saved settings
		SharedPreferences prefs = getUserSettings();
		if (prefs != null)
			prefs.edit().remove(Constants.PERSISTENCE_PREF_UID).commit();

		// Forgot info about last user
		mPersistence.saveLastEmail(null);
	}

	/**
	 * Checks if user is logged in (has session UID).
	 * 
	 * @return true if user is logged in, false otherwise
	 */
	public boolean isLoggedIn() {
		// TODO: Check session lifetime somehow?
		return !mHousehold.user.getEmail().isEmpty() && !mNetwork.getUID().isEmpty();
	}

	/**
	 * Checks if Internet connection is available.
	 * 
	 * @return true if available, false otherwise
	 */
	public boolean isInternetAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public void beginPersistentConnection() {
		if (mNetwork instanceof Network)
			((Network) mNetwork).multiSessionBegin();
	}

	public void endPersistentConnection() {
		if (mNetwork instanceof Network)
			((Network) mNetwork).multiSessionEnd();
	}

	/** Reloading data methods **********************************************/

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadAdapters(boolean forceReload) {
		if (!isLoggedIn()) {
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
		if (!isLoggedIn()) {
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
		if (!isLoggedIn()) {
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
		if (!isLoggedIn()) {
			return false;
		}

		return mHousehold.uninitializedFacilitiesModel.reloadUninitializedFacilitiesByAdapter(adapterId, forceReload);
	}

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param pair
	 * @return
	 */
	public synchronized boolean reloadDeviceLog(LogDataPair pair) {
		if (!isLoggedIn()) {
			return false;
		}

		return mHousehold.deviceLogsModel.reloadDeviceLog(pair);
	}

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param facility
	 * @return
	 */
	public boolean updateFacility(Facility facility, boolean forceReload) {
		if (!isLoggedIn()) {
			return false;
		}

		return mHousehold.facilitiesModel.refreshFacility(facility, forceReload);
	}

	/**
	 * This CAN'T be called on UI thread!
	 * 
	 * @param facilities
	 * @return
	 */
	public boolean updateFacilities(List<Facility> facilities, boolean forceReload) {
		if (!isLoggedIn()) {
			return false;
		}

		return mHousehold.facilitiesModel.refreshFacilities(facilities, forceReload);
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
				prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, mHousehold.activeAdapter.getId())
						.commit();
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
	 * Registers new adapter to server. Automatically reloads list of adapters, set this adapter as active and load all
	 * its sensors.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param id
	 * @return true on success, false otherwise
	 */
	public boolean registerAdapter(String id, String adapterName) {
		boolean result = false;

		try {
			if (mNetwork.addAdapter(id, adapterName)) {
				mHousehold.adaptersModel.reloadAdapters(true);
				setActiveAdapter(id, true);
				result = true;
			}
		} catch (AppException e) {
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
	// public boolean registerUser(String email) {
	// boolean result = false;
	//
	// try {
	// result = mNetwork.signUp(mHousehold.user.getEmail()); //FIXME: ROB use getUID instead!
	// } catch (AppException e) {
	// e.printStackTrace();
	// }
	//
	// return result;
	// }

	/**
	 * FIXME: debug implementation Unregisters adapter from server.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param id
	 * @return true on success, false otherwise
	 */
	public boolean unregisterAdapter(String id) {
		// FIXME: This debug implementation unregisters actual user from adapter, not adapter itself

		boolean result = false;

		try {
			if (mNetwork.deleteAccount(id, mHousehold.user)) {
				if (mHousehold.activeAdapter != null && mHousehold.activeAdapter.getId().equals(id))
					mHousehold.activeAdapter = null;

				mHousehold.adaptersModel.reloadAdapters(true);
				result = true;
			}
		} catch (AppException e) {
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
			deleted = mNetwork.deleteLocation(adapter.getId(), location);
		} catch (AppException e) {
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
			saved = mNetwork.updateLocation(adapter.getId(), location);
		} catch (AppException e) {
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
			location = mNetwork.createLocation(adapter.getId(), location);
		} catch (AppException e) {
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

	public Device getDevice(String adapterId, String id) {
		String[] ids = id.split(Device.ID_SEPARATOR, 2);

		Facility facility = getFacility(adapterId, ids[0]);
		if (facility == null)
			return null;

		int iType = Integer.parseInt(ids[1]);
		DeviceType type = DeviceType.fromValue(iType);

		return facility.getDeviceByType(type);
	}

	/**
	 * Return list of all uninitialized facilities from adapter
	 * 
	 * @param adapterId
	 * @param withIgnored
	 * @return List of uninitialized facilities (or empty list)
	 */
	public List<Facility> getUninitializedFacilities(String adapterId) {
		return mHousehold.uninitializedFacilitiesModel.getUninitializedFacilitiesByAdapter(adapterId);
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
	public boolean saveDevice(Device device, EnumSet<SaveDevice> what) {
		return mHousehold.facilitiesModel.saveDevice(device, what);
	}

    /**
     * Delete facility
     *
     */
    public boolean delFacility(Facility facility) {
        return mHousehold.facilitiesModel.delFacility(facility);
    }

	/**
	 * Return log for device.
	 * 
	 * @param device
	 * @return
	 */
	public DeviceLog getDeviceLog(LogDataPair pair) {
		return mHousehold.deviceLogsModel.getDeviceLog(pair);
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
		// FIXME: hack -> true if you want to add virtual sensor
		boolean result = false;

		try {
			result = mNetwork.prepareAdapterToListenNewSensors(adapterID);
		} catch (AppException e) {
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
	
	public Boolean reloadAdapterUsers(String adapterId, boolean mForceReload) {
		mRequestUsers = mNetwork.getAccounts(adapterId);
		return true;
	}
	
	public List<User> getUsers() {
		if(mRequestUsers != null)
			return  mRequestUsers;
		return null;
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
		return mNetwork.addAccount(adapterId, user);
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
		return mNetwork.deleteAccount(adapterId, user);
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
		return mNetwork.updateAccount(adapterId, user);
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * This CAN'T be called on UI thread!
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
			// delete actual GCM ID from server
			ActualUser user;
			if ((user = getActualUser()) != null) {
				try {
					deleteGCM(user.getEmail(), registrationId);
				} catch (Exception e) {
					// do nothing
					Log.w(GcmHelper.TAG_GCM, "getGCMRegistrationId(): Delete GCM ID failed: " + e.getLocalizedMessage());
				}
			}
			mPersistence.saveGCMRegistrationId("");
			Log.i(TAG, "GCM: App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Delete GCM ID from user
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param email
	 * @param gcmID
	 */
	public void deleteGCM(String email, String gcmID) {
		mNetwork.deleteGCMID(email, gcmID);
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 * 
	 * @param gcmId
	 *            registration ID
	 */
	public void setGCMIdLocal(String gcmId) {
		int appVersion = Utils.getAppVersion(mContext);
		Log.i(TAG, "Saving GCM ID on app version " + appVersion);

		mPersistence.saveGCMRegistrationId(gcmId);
		mPersistence.saveLastApplicationVersion(appVersion);
	}

	/**
	 * Method set gcmID to server (applied only if there is some user)
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param gcmID
	 *            to be set
	 */
	public void setGCMIdServer(String gcmID) {
		String email;
		Log.i(GcmHelper.TAG_GCM, "setGcmIdServer");
		if (isDemoMode()) {
			Log.i(GcmHelper.TAG_GCM, "DemoMode -> return");
			return;
		}
		
		if (getActualUser() != null) {
			email = getActualUser().getEmail();
		} else if (!getLastEmail().isEmpty()) {
			email = getLastEmail();
		} else {
			// no user, it will be sent in user login
			return;
		}

		try {
			Log.i(GcmHelper.TAG_GCM, "Set GCM ID to server: " + gcmID);
			mNetwork.setGCMID(email, gcmID);
		} catch (Exception e) {
			// nothing to do
			Log.e(GcmHelper.TAG_GCM, "Set GCM ID to server failed.");
		}
	}

	public ActualUser getActualUser() {
		return mHousehold.user;
	}

	/**
	 * Send request to server to switch Actor value.
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param device
	 *            DeviceType of this device must be actor, i.e., device.getType().isActor() must return true.
	 * @return true on success, false otherwise
	 */
	public Boolean switchActorValue(Device device) {
		return mHousehold.facilitiesModel.switchActor(device);
	}

	/** Notification methods ************************************************/

	/**
	 * Register receiver for receiving new notifications.
	 * 
	 * @param receiver
	 */
	public void registerNotificationReceiver(INotificationReceiver receiver) {
		mNotificationReceivers.put(receiver, true);
	}

	/**
	 * Unregister listener from receiving new notifications.
	 * 
	 * @param receiver
	 */
	public void unregisterNotificationReceiver(INotificationReceiver receiver) {
		mNotificationReceivers.remove(receiver);
	}

	/**
	 * Sends Notification to all registered receivers.
	 * 
	 * <br>
	 * NOTE: This should be called by some GcmHandler only. Or maybe this should be inside of that class directly and
	 * Controller should "redirect" (un)registering for calling it there too.
	 * 
	 * @param notification
	 * @return
	 */
	public int receiveNotification(Notification notification) {
		for (INotificationReceiver receiver : mNotificationReceivers.keySet()) {
			receiver.receiveNotification(notification);
		}

		return mNotificationReceivers.size();
	}

	/**
	 * Set notification as read on server side
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param msgId
	 */
	public void setNotificationRead(String msgId) {
		ArrayList<String> list = new ArrayList<String>();
		list.add(msgId);
		setNotificationRead(list);
	}

	/**
	 * Set notifications as read on server side
	 * 
	 * This CAN'T be called on UI thread!
	 * 
	 * @param msgIds
	 *            Array of message IDs which will be marked as read
	 */
	public void setNotificationRead(ArrayList<String> msgIds) {
		mNetwork.NotificationsRead(msgIds);
	}

	
	/**
	 * UCA
	 */
	public boolean isUserAllowed(Role role) {
		if(role.equals(Role.User) ||role.equals(Role.Guest) ){
			return false;
		}
		return true;
	}
	 
	

}