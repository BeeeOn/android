package com.rehivetech.beeeon.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.NotImplementedException;
import com.rehivetech.beeeon.gcm.GcmHelper;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.Notification;
import com.rehivetech.beeeon.geofence.TransitionType;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Role;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.network.DemoNetwork;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.Network;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.persistence.AdaptersModel;
import com.rehivetech.beeeon.persistence.DeviceLogsModel;
import com.rehivetech.beeeon.persistence.FacilitiesModel;
import com.rehivetech.beeeon.persistence.GeofenceModel;
import com.rehivetech.beeeon.persistence.LocationsModel;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.persistence.UninitializedFacilitiesModel;
import com.rehivetech.beeeon.persistence.WatchDogsModel;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Core of application (used as singleton), provides methods and access to all data and household.
 * 
 * @author Robyer
 */
public final class Controller {

	public static final String TAG = Controller.class.getSimpleName();

	/** This singleton instance. */
	private static Controller sController;

	/** Switch for using demo mode (with example adapter, without server) */
	private static boolean sDemoMode = false;

	/** Application context */
	private final Context mContext;

	/** Persistence service for caching purposes */
	private final Persistence mPersistence;

	/** Network service for communication with server */
	private final INetwork mNetwork;

	/** Weak map for holding registered notification receivers */
	private final WeakHashMap<INotificationReceiver, Boolean> mNotificationReceivers = new WeakHashMap<INotificationReceiver, Boolean>();

	private List<User> mRequestUsers;

	/** Active user object */
	private final User mUser;

	/** Active adapter */
	private Adapter mActiveAdapter;

	/** Models for keeping and handling data */
	private final AdaptersModel mAdaptersModel;
	private final LocationsModel mLocationsModel;
	private final FacilitiesModel mFacilitiesModel;
	private final UninitializedFacilitiesModel mUninitializedFacilitiesModel;
	private final DeviceLogsModel mDeviceLogsModel;
	private final WatchDogsModel mWatchDogsModel;
	private final GeofenceModel mGeofenceModel;

	/**
	 * Return singleton instance of this Controller. This is thread-safe.
	 * 
	 * @param context
	 * @return singleton instance of controller
	 */
	public static Controller getInstance(Context context) {
		if (sController == null) {
			synchronized (Controller.class) {
				if (sController == null) {
					sController = new Controller(context.getApplicationContext());
				}
			}
		}

		return sController;
	}

	/**
	 * Private constructor.
	 * 
	 * @param context
	 *            This must be the global application context.
	 */
	private Controller(Context context) {
		mContext = context;

		mNetwork = sDemoMode ? new DemoNetwork(mContext) : new Network(mContext, Utils.isDebugVersion(mContext));
		mPersistence = new Persistence(mContext);
		mUser = new User();

		// Create models
		mAdaptersModel = new AdaptersModel(mNetwork);
		mLocationsModel = new LocationsModel(mNetwork, mContext);
		mFacilitiesModel = new FacilitiesModel(mNetwork);
		mUninitializedFacilitiesModel = new UninitializedFacilitiesModel(mNetwork);
		mDeviceLogsModel = new DeviceLogsModel(mNetwork);
		mWatchDogsModel = new WatchDogsModel(mNetwork);
		mGeofenceModel = new GeofenceModel(mContext);

		// Load previous user
		String userId = mPersistence.loadLastUserId();
		if (!userId.isEmpty()) {
			mUser.setId(userId);
			// Load rest of user details (if available)
			mPersistence.loadUserDetails(userId, mUser);
			// Finally load BT (session) - we can call it directly like that because here we doesn't care whether it's empty because it's empty since Network creation
			mNetwork.setBT(mPersistence.loadLastBT(userId));
		}
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
		sDemoMode = demoMode;
		sController = new Controller(context);
	}

	public static boolean isDemoMode() {
		return sDemoMode;
	}

	/** Model getters *******************************************************/

	public GeofenceModel getGeofenceModel() {
		return mGeofenceModel;
	}

	public AdaptersModel getAdaptersModel() {
		return mAdaptersModel;
	}

	public LocationsModel getLocationsModel() {
		return mLocationsModel;
	}

	public FacilitiesModel getFacilitiesModel() {
		return mFacilitiesModel;
	}

	public UninitializedFacilitiesModel getUninitializedFacilitiesModel() {
		return mUninitializedFacilitiesModel;
	}

	public DeviceLogsModel getDeviceLogsModel() {
		return mDeviceLogsModel;
	}

	public WatchDogsModel getWatchDogsModel() {
		return mWatchDogsModel;
	}

	/** Persistence methods *************************************************/

	public IAuthProvider getLastAuthProvider() {
		return mPersistence.loadLastAuthProvider();
	}

	public String getLastUserId() {
		return mPersistence.loadLastUserId();
	}

	/**
	 * Get SharedPreferences for actually logged in user
	 *
	 * @return null if user is not logged in
	 */
	public SharedPreferences getUserSettings() {
		String userId = mUser.getId();
		if (userId.isEmpty()) {
			Log.e(TAG, "getUserSettings() with no loaded userId");
			return null;
		}

		return mPersistence.getSettings(userId);
	}

	/**
	 * Get global SharedPreferences for whole application
	 */
	public SharedPreferences getGlobalSettings() {
		return mPersistence.getSettings(Persistence.GLOBAL);
	}

	/** Communication methods ***********************************************/

	/**
	 * Load user data from server and save them to cache.
	 *
	 * @param userId can be null when this is first login
	 */
	public void loadUserData(String userId) {
		// Load cached user details, if this is not first login
		if (userId != null) {
			mPersistence.loadUserDetails(userId, mUser);
		}

		// Load user data from server
		User user = mNetwork.loadUserInfo();

		// Eventually save correct userId and download picture if changed (but not in demoMode)
		if (!(mNetwork instanceof DemoNetwork)) {
			if (!user.getId().equals(userId)) {
				// UserId from server is not same as the cached one (or this is first login)
				if (userId != null) {
					Log.e(TAG, String.format("UserId from server (%s) is not same as the cached one (%s).", user.getId(), userId));
				} else {
					Log.d(TAG, String.format("Loaded userId from server (%s), this is first login.", user.getId()));
				}
				// So save the correct userId
				mPersistence.saveLastUserId(user.getId());
			}

			// If we have no or changed picture, lets download it from server
			if (!user.getPictureUrl().isEmpty() && (user.getPicture() == null || !mUser.getPictureUrl().equals(user.getPictureUrl()))) {
				Bitmap picture = Utils.fetchImageFromUrl(user.getPictureUrl());
				user.setPicture(picture);
			}
		}

		// Copy user data
		mUser.setId(user.getId());
		mUser.setRole(user.getRole());
		mUser.setName(user.getName());
		mUser.setSurname(user.getSurname());
		mUser.setGender(user.getGender());
		mUser.setEmail(user.getEmail());
		mUser.setPictureUrl(user.getPictureUrl());
		mUser.setPicture(user.getPicture());

		// We have fresh user details, save them to cache (but not in demoMode)
		if (!(mNetwork instanceof DemoNetwork)) {
			mPersistence.saveUserDetails(user.getId(), mUser);
		}
	}

	/**
	 * Login user with any authProvider (authenticate on server).
	 *
	 * @param authProvider
	 * @return true on success, false otherwise
	 * @throws AppException
	 */
	public boolean login(IAuthProvider authProvider) throws AppException {
		// In demo mode load some init data from sdcard
		if (mNetwork instanceof DemoNetwork) {
			mGeofenceModel.deleteDemoData();
			((DemoNetwork) mNetwork).initDemoData();
		}

		// We don't have beeeon-token yet, try to login
		mNetwork.loginMe(authProvider); // throws exception on error

		// Load user data so we will know our userId
		loadUserData(null);

		// Do we have session now?
		if (mNetwork.hasBT()) {
			Log.e(TAG, "BeeeOn token wasn't received. We are not logged in.");
			return false;
		}

		String userId = mUser.getId();
		if (userId.isEmpty()) {
			Log.e(TAG, "UserId wasn't received. We can't continue with login.");
			return false;
		}

		// Save our new BT
		String bt = mNetwork.getBT();
		Log.i(TAG, String.format("Loaded for user '%s' fresh new BT: %s", userId, bt));
		mPersistence.saveLastBT(userId, bt);

		// Then remember this user
		mPersistence.initializeDefaultSettings(userId);

		// Remember this email to use with auto login (but not in demoMode)
		if (!(mNetwork instanceof DemoNetwork)) {
			mPersistence.saveLastUserId(mUser.getId());
			mPersistence.saveLastAuthProvider(authProvider);

			registerGCM();
		}

		return true;
	}

	/**
	 * Register user with any authProvider (authenticate on server).
	 *
	 * @param authProvider
	 * @return true on success, false otherwise
	 * @throws AppException
	 */
	public boolean register(IAuthProvider authProvider) throws AppException {
		// We don't have beeeon-token yet, try to login
		return mNetwork.registerMe(authProvider); // throws exception on error
	}

	/**
	 * Destroy user session in network and forget him as last logged in user.
	 */
	public void logout() {
		// TODO: Request to logout from server (discard actual communication UID)

		// Delete GCM ID on server side
		if (!(mNetwork instanceof DemoNetwork)) {
			final String id = mUser.getId();
			final String gcmId = getGCMRegistrationId();
			if (!id.isEmpty() && !gcmId.isEmpty()) {
				// delete GCM ID from server
				Thread t = new Thread() {
					public void run() {
						try {
							deleteGCM(id, gcmId);
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
		mNetwork.setBT("");

		// Delete session from saved settings
		SharedPreferences prefs = getUserSettings();
		if (prefs != null)
			prefs.edit().remove(Constants.PERSISTENCE_PREF_USER_BT).commit();

		// Forgot info about last user
		mPersistence.saveLastAuthProvider(null);
		mPersistence.saveLastUserId(null);
	}

	/**
	 * Checks if user is logged in (Network has beeeon-token for communication).
	 *
	 * @return true if user is logged in, false otherwise
	 */
	public boolean isLoggedIn() {
		return mNetwork.hasBT();
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
		return mAdaptersModel.reloadAdapters(forceReload);
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadLocations(String adapterId, boolean forceReload) {
		return mLocationsModel.reloadLocationsByAdapter(adapterId, forceReload);
	}

	/**
	 * Return all adapters that this logged in user has access to.
	 *
	 * @return List of adapters
	 */
	public List<Adapter> getAdapters() {
		return mAdaptersModel.getAdapters();
	}

	/**
	 * Return adapter by his ID.
	 *
	 * @param id
	 * @return Adapter if found, null otherwise
	 */
	public Adapter getAdapter(String id) {
		return mAdaptersModel.getAdapter(id);
	}

	/**
	 * Return active adapter.
	 *
	 * @return active adapter, or first adapter, or null if there are no adapters
	 */
	public synchronized Adapter getActiveAdapter() {
		if (mActiveAdapter == null) {
			// UserSettings can be null when user is not logged in!
			SharedPreferences prefs = getUserSettings();

			String lastId = (prefs == null) ? "" : prefs.getString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, "");

			Map<String, Adapter> adapters = mAdaptersModel.getAdaptersMap();
			if (!lastId.isEmpty() && adapters.containsKey(lastId)) {
				mActiveAdapter = adapters.get(lastId);
			} else {
				for (Adapter adapter : adapters.values()) {
					mActiveAdapter = adapter;
					break;
				}
			}

			if (mActiveAdapter != null && prefs != null)
				prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, mActiveAdapter.getId())
						.commit();
		}

		return mActiveAdapter;
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
		Map<String, Adapter> adapters = mAdaptersModel.getAdaptersMap();
		if (!adapters.containsKey(id)) {
			Log.d(TAG, String.format("Can't set active adapter to '%s'", id));
			return false;
		}

		Adapter adapter = adapters.get(id);
		mActiveAdapter = adapter;
		Log.d(TAG, String.format("Set active adapter to '%s'", adapter.getName()));

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = getUserSettings();
		if (prefs != null) {
			prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, adapter.getId()).commit();
		}

		// Load locations and facilities, if needed
		reloadLocations(id, forceReload);
		mFacilitiesModel.reloadFacilitiesByAdapter(id, forceReload);

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
	public boolean registerAdapter(String id, String adapterName){
		boolean result = false;

		if (mNetwork.addAdapter(id, adapterName)) {
			mAdaptersModel.reloadAdapters(true);
			setActiveAdapter(id, true);
			result = true;
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
	public boolean unregisterAdapter(String id) {
		// FIXME: This debug implementation unregisters actual user from adapter, not adapter itself

		if (mNetwork.deleteAccount(id, mUser)) {
			if (mActiveAdapter != null && mActiveAdapter.getId().equals(id))
				mActiveAdapter = null;

			mAdaptersModel.reloadAdapters(true);
			return true;
		}

		return false;
	}

	/** Location methods ****************************************************/

	/**
	 * Return location from active adapter by id.
	 *
	 * @param id
	 * @return Location if found, null otherwise.
	 */
	public Location getLocation(String adapterId, String id) {
		return mLocationsModel.getLocation(adapterId, id);
	}

	/**
	 * Return list of locations from active adapter.
	 *
	 * @return List of locations (or empty list)
	 */
	public List<Location> getLocations(String adapterId) {
		return mLocationsModel.getLocationsByAdapter(adapterId);
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

		boolean deleted = mNetwork.deleteLocation(adapter.getId(), location);

		// Location was deleted on server, remove it from adapter too
		return deleted && mLocationsModel.deleteLocation(adapter.getId(), location.getId());
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

		boolean saved = mNetwork.updateLocation(adapter.getId(), location);

		// Location was updated on server, update it to adapter too
		return saved && mLocationsModel.updateLocation(adapter.getId(), location);
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

		location = mNetwork.createLocation(adapter.getId(), location);

		// Location was saved on server, save it to adapter too
		return (location != null && mLocationsModel.addLocation(adapter.getId(), location)) ? location : null;
	}

	/**
	 * Send pair request
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @return result
	 */
	public boolean sendPairRequest(String adapterId) {
		// FIXME: hack -> true if you want to add virtual sensor
		return mNetwork.prepareAdapterToListenNewSensors(adapterId);
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


	// FIXME: all these user methods move to model!!!
	public Boolean reloadAdapterUsers(String adapterId, boolean mForceReload) {
		mRequestUsers = mNetwork.getAccounts(adapterId);
		return true;
	}

	public List<User> getUsers() {
		if(mRequestUsers != null)
			return  mRequestUsers;
		return new ArrayList<User>();
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

	private void registerGCM() {
		// Send GCM ID to server
		final String gcmId = sController.getGCMRegistrationId();
		if (gcmId.isEmpty()) {
			GcmHelper.registerGCMInBackground(mContext);
			Log.w(GcmHelper.TAG_GCM, "GCM ID is not accessible in persistence, creating new thread");
		} else {
			// send GCM ID to server
			Thread t = new Thread() {
				public void run() {
					try {
						sController.setGCMIdServer(gcmId);
					} catch (Exception e) {
						// do nothing
						Log.w(GcmHelper.TAG_GCM, "Login: Sending GCM ID to server failed: " + e.getLocalizedMessage());
					}
				}
			};
			t.start();
			sController.setGCMIdLocal(gcmId);
		}
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
			try {
				deleteGCM(mUser.getId(), registrationId);
			} catch (Exception e) {
				// do nothing
				Log.w(GcmHelper.TAG_GCM, "getGCMRegistrationId(): Delete GCM ID failed: " + e.getLocalizedMessage());
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
	 * @param userId
	 * @param gcmID
	 */
	public void deleteGCM(String userId, String gcmID) {
		if (mNetwork instanceof Network) {
			((Network) mNetwork).deleteGCMID(userId, gcmID);
		}
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
		Log.i(GcmHelper.TAG_GCM, "setGcmIdServer");
		if (sDemoMode) {
			Log.i(GcmHelper.TAG_GCM, "DemoMode -> return");
			return;
		}

		if (mUser.getId().isEmpty()) {
			// no user, it will be sent in user login
			return;
		}

		try {
			Log.i(GcmHelper.TAG_GCM, "Set GCM ID to server: " + gcmID);
			if (mNetwork instanceof Network) {
				((Network) mNetwork).setGCMID(gcmID);
			}
		} catch (Exception e) {
			// nothing to do
			Log.e(GcmHelper.TAG_GCM, "Set GCM ID to server failed.");
		}
	}

	public User getActualUser() {
		return mUser;
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
		if (role.equals(Role.User) ||role.equals(Role.Guest)) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param geofenceId Geofence ID which is unique per user for all devices
	 * @param type
	 */
	public void setPassBorder(String geofenceId, TransitionType type) {
		Log.i(TAG, "Passing geofence and seding to server");
		mNetwork.passBorder(geofenceId, type.getString());
	}

	/**
	 * Either edits or creates new watchdog
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param watchdog
	 * @return
	 */
	// FIXME: Tom, move this into WatchDogsModel and then use call via controller.getWatchDogsModel().yourMethod();
	public boolean saveWatchDog(WatchDog watchdog) {
		Adapter adapter = getActiveAdapter();
		if (adapter == null) {
			return false;
		}

		// TODO should it be here?
		watchdog.setAdapterId(adapter.getId());

		// if watchdog has ID, edit id
		if(watchdog.getId() != null){
			// first tries to update on server, then in persistence
			return mNetwork.updateWatchDog(watchdog, adapter.getId()) && mWatchDogsModel.updateWatchDog(adapter.getId(), watchdog);
		}
		else{
			// first tries to add on server, then in persistence
			return mNetwork.addWatchDog(watchdog, adapter.getId()) && mWatchDogsModel.addWatchDog(adapter.getId(), watchdog);
		}
	}

	/**
	 * Delete a watchdog
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param watchdog
	 * @return
	 */
	// FIXME: Tom, move this into WatchDogsModel and then use call via controller.getWatchDogsModel().yourMethod();
	public boolean deleteWatchDog(WatchDog watchdog) {
		Adapter adapter = getActiveAdapter();
		if (adapter == null) {
			return false;
		}
		// delete from server
		boolean deleted = mNetwork.deleteWatchDog(watchdog);
		// watchdog was deleted on server, remove it from adapter too
		return deleted && mWatchDogsModel.deleteWatchDog(adapter.getId(), watchdog.getId());
	}
}