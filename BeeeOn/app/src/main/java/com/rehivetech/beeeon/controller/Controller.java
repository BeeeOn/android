package com.rehivetech.beeeon.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Role;
import com.rehivetech.beeeon.network.DemoNetwork;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.Network;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.persistence.AdaptersModel;
import com.rehivetech.beeeon.persistence.DeviceLogsModel;
import com.rehivetech.beeeon.persistence.FacilitiesModel;
import com.rehivetech.beeeon.persistence.GcmModel;
import com.rehivetech.beeeon.persistence.GeofenceModel;
import com.rehivetech.beeeon.persistence.LocationsModel;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.persistence.UninitializedFacilitiesModel;
import com.rehivetech.beeeon.persistence.UsersModel;
import com.rehivetech.beeeon.persistence.WatchDogsModel;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import java.util.Map;

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
	private final GcmModel mGcmModel;
	private final UsersModel mUsersModel;

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

		// In demo mode immediately load user data
		if (sDemoMode) {
			loadUserData(DemoNetwork.DEMO_USER_ID);
		}

		// Create models
		mAdaptersModel = new AdaptersModel(mNetwork);
		mLocationsModel = new LocationsModel(mNetwork, mContext);
		mFacilitiesModel = new FacilitiesModel(mNetwork);
		mUninitializedFacilitiesModel = new UninitializedFacilitiesModel(mNetwork);
		mDeviceLogsModel = new DeviceLogsModel(mNetwork);
		mWatchDogsModel = new WatchDogsModel(mNetwork);
		mGeofenceModel = new GeofenceModel(mNetwork, mContext);
		mGcmModel = new GcmModel(mContext, mNetwork, mPersistence, mUser);
		mUsersModel = new UsersModel(mNetwork);

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

	public GcmModel getGcmModel() {
		return mGcmModel;
	}

	public UsersModel getUsersModel() {
		return mUsersModel;
	}

	/** Persistence methods *************************************************/

	public IAuthProvider getLastAuthProvider() {
		return mPersistence.loadLastAuthProvider();
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
		if (!mNetwork.hasBT()) {
			Log.e(TAG, "BeeeOn token wasn't received. We are not logged in.");
			return false;
		}

		String userId = mUser.getId();
		if (userId.isEmpty()) {
			Log.e(TAG, "UserId wasn't received. We can't continue with login.");
			return false;
		}

		// Then initialize default settings
		mPersistence.initializeDefaultSettings(userId);

		if (!(mNetwork instanceof DemoNetwork)) {
			// Save our new BT
			String bt = mNetwork.getBT();
			Log.i(TAG, String.format("Loaded for user '%s' fresh new BT: %s", userId, bt));
			mPersistence.saveLastBT(userId, bt);

			// Remember this email to use with auto login
			mPersistence.saveLastUserId(mUser.getId());
			mPersistence.saveLastAuthProvider(authProvider);

			mGcmModel.registerGCM();
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
		// TODO: Request to logout from server (discard actual BT)

		// Delete GCM id on server side
		mGcmModel.deleteGCM(mUser.getId(), null);

		// Destroy session
		mNetwork.setBT("");

		// Delete session from saved settings
		SharedPreferences prefs = getUserSettings();
		if (prefs != null)
			prefs.edit().remove(Constants.PERSISTENCE_PREF_USER_BT).apply();

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

			mActiveAdapter = mAdaptersModel.getAdapterOrFirst(lastId);

			if (mActiveAdapter != null && prefs != null)
				prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, mActiveAdapter.getId()).apply();
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
		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = getUserSettings();
		if (prefs != null) {
			// Save it whether adapter below will be loaded or not
			prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, id).apply();
		}

		// Find specified adapter
		mActiveAdapter = mAdaptersModel.getAdapter(id);

		if (mActiveAdapter == null) {
			Log.d(TAG, String.format("Can't set active adapter to '%s'", id));
			return false;
		}

		Log.d(TAG, String.format("Set active adapter to '%s'", mActiveAdapter.getName()));

		// Load locations and facilities, if needed
		mLocationsModel.reloadLocationsByAdapter(id, forceReload);
		mFacilitiesModel.reloadFacilitiesByAdapter(id, forceReload);

		return true;
	}

	public User getActualUser() {
		return mUser;
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

}