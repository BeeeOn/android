package com.rehivetech.beeeon.controller;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Role;
import com.rehivetech.beeeon.model.AchievementsModel;
import com.rehivetech.beeeon.model.AdaptersModel;
import com.rehivetech.beeeon.model.BaseModel;
import com.rehivetech.beeeon.model.DeviceLogsModel;
import com.rehivetech.beeeon.model.FacilitiesModel;
import com.rehivetech.beeeon.model.GcmModel;
import com.rehivetech.beeeon.model.GeofenceModel;
import com.rehivetech.beeeon.model.LocationsModel;
import com.rehivetech.beeeon.model.UninitializedFacilitiesModel;
import com.rehivetech.beeeon.model.UsersModel;
import com.rehivetech.beeeon.model.WatchDogsModel;
import com.rehivetech.beeeon.network.DemoNetwork;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.Network;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private final boolean mDemoMode;

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
	private final Map<String, BaseModel> mModels = new HashMap<>();

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
					// Load last used mode
					boolean demoMode = Persistence.loadLastDemoMode(context);

					// Create new singleton instance of controller
					sController = new Controller(context, demoMode);
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
	 * @param demoMode
	 *            Whether should be created Controller in demoMode
	 */
	private Controller(Context context, boolean demoMode) {
		mContext = context.getApplicationContext();
		mDemoMode = demoMode;

		// Create basic objects
		mNetwork = mDemoMode ? new DemoNetwork(mContext) : new Network(mContext);
		mPersistence = new Persistence(mContext);
		mUser = new User();

		// In demo mode immediately load user data
		if (mDemoMode) {
			loadUserData(DemoNetwork.DEMO_USER_ID);
		}

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
		// Remember last used mode
		Persistence.saveLastDemoMode(context, demoMode);

		// We always need to create a new Controller, due to account switch and first (not) loading of demo
		sController = new Controller(context, demoMode);
	}

	public boolean isDemoMode() {
		return mDemoMode;
	}

	/** Model getters *******************************************************/

	/**
	 * Method return instance of given Model class.
	 * Returns existing instance if already exists, otherwise tries to instantiate new instance, which remembers.
	 * NOTE: Internal instantiation supports only few object as Model class constructor's parameters.
	 *
	 * @param modelClass
	 * @return
	 */
	public BaseModel getModelInstance(Class<? extends BaseModel> modelClass) {
		final String name = modelClass.getName();

		if (!mModels.containsKey(name)) {
			synchronized (mModels) {
				if (!mModels.containsKey(name)) {
					// Known parameters we can automatically give to model constructor
					final Object[] supportedParams = { mNetwork, mContext, mPersistence, mUser };

					// Create instance of the given model class
					final Constructor constructor = modelClass.getConstructors()[0];
					final List<Object> params = new ArrayList<>();
					for (Class<?> pType : constructor.getParameterTypes())
					{
						Object param = null;
						for (Object obj : supportedParams) {
							if (pType.isInstance(obj)) {
								param = obj;
								break;
							}
						}

						if (param == null) {
							String error = String.format("Unsupported model parameter type (%s) for automatic model construction.", pType.getSimpleName());
							throw new UnsupportedOperationException(error);
						}

						params.add(param);
					}

					try {
						// Try to create new model instance
						final BaseModel model = (BaseModel) constructor.newInstance(params.toArray());

						// Put this created model to map
						mModels.put(name, model);
					} catch (Exception e) {
						// NOTE: Catching base Exception because of Android's merging more exceptions into one, which is not supported before API 19
						e.printStackTrace();
					}
				}
			}
		}

		return mModels.get(name);
	}

	public GeofenceModel getGeofenceModel() {
		return (GeofenceModel) getModelInstance(GeofenceModel.class);
	}

	public AdaptersModel getAdaptersModel() {
		return (AdaptersModel) getModelInstance(AdaptersModel.class);
	}

	public AchievementsModel getAchievementsModel() {
		return (AchievementsModel) getModelInstance(AchievementsModel.class);
	}

	public LocationsModel getLocationsModel() {
		return (LocationsModel) getModelInstance(LocationsModel.class);
	}

	public FacilitiesModel getFacilitiesModel() {
		return (FacilitiesModel) getModelInstance(FacilitiesModel.class);
	}

	public UninitializedFacilitiesModel getUninitializedFacilitiesModel() {
		return (UninitializedFacilitiesModel) getModelInstance(UninitializedFacilitiesModel.class);
	}

	public DeviceLogsModel getDeviceLogsModel() {
		return (DeviceLogsModel) getModelInstance(DeviceLogsModel.class);
	}

	public WatchDogsModel getWatchDogsModel() {
		return (WatchDogsModel) getModelInstance(WatchDogsModel.class);
	}

	public GcmModel getGcmModel() {
		return (GcmModel) getModelInstance(GcmModel.class);
	}

	public UsersModel getUsersModel() {
		return (UsersModel) getModelInstance(UsersModel.class);
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
			getGeofenceModel().deleteDemoData();
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

			getGcmModel().registerGCM();
		}

		// Register geofence areas asynchronously
		Log.i(TAG, "Geofence: Starting registering Geofence");
		getGeofenceModel().registerAllUserGeofence(userId);

		// send logout broadcast so widget can set cached
		mContext.sendBroadcast(new Intent(Constants.BROADCAST_USER_LOGIN));

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

		// delete geofences
		getGeofenceModel().unregisterAllUserGeofence(getActualUser().getId());

		// delete all visible notification
		NotificationManager notifMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notifMgr.cancelAll();

		// delete all achievements
		AchievementList.cleanAll();

		// Delete GCM id on server side
		getGcmModel().deleteGCM(mUser.getId(), null);

		// Destroy session
		mNetwork.setBT("");

		// Delete session from saved settings
		SharedPreferences prefs = getUserSettings();
		if (prefs != null)
			prefs.edit().remove(Constants.PERSISTENCE_PREF_USER_BT).apply();

		// Forgot info about last user
		Persistence.saveLastDemoMode(mContext, null);
		mPersistence.saveLastAuthProvider(null);
		mPersistence.saveLastUserId(null);

		// send logout broadcast so widget can set cached
		mContext.sendBroadcast(new Intent(Constants.BROADCAST_USER_LOGOUT));
	}

	/**
	 * Checks if user is logged in (Network has beeeon-token for communication).
	 *
	 * @return true if user is logged in, false otherwise
	 */
	public boolean isLoggedIn() {
		return mNetwork.hasBT();
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

			mActiveAdapter = getAdaptersModel().getAdapterOrFirst(lastId);

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
		mActiveAdapter = getAdaptersModel().getAdapter(id);

		if (mActiveAdapter == null) {
			Log.d(TAG, String.format("Can't set active adapter to '%s'", id));
			return false;
		}

		Log.d(TAG, String.format("Set active adapter to '%s'", mActiveAdapter.getName()));

		// Load locations and facilities, if needed
		getLocationsModel().reloadLocationsByAdapter(id, forceReload);
		getFacilitiesModel().reloadFacilitiesByAdapter(id, forceReload);

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

	/**
	 * Interrupts actual connection (opened socket) of Network module.
	 */
	public void interruptConnection() {
		if (mNetwork instanceof Network) {
			((Network) mNetwork).interruptConnection();
		}
	}

}