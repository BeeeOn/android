package com.rehivetech.beeeon.controller;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gcm.GcmRegistrationIntentService;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Role;
import com.rehivetech.beeeon.model.BaseModel;
import com.rehivetech.beeeon.model.DevicesModel;
import com.rehivetech.beeeon.model.GatesModel;
import com.rehivetech.beeeon.model.GcmModel;
import com.rehivetech.beeeon.model.LocationsModel;
import com.rehivetech.beeeon.model.ModuleLogsModel;
import com.rehivetech.beeeon.model.UninitializedDevicesModel;
import com.rehivetech.beeeon.model.UsersModel;
import com.rehivetech.beeeon.model.WeatherModel;
import com.rehivetech.beeeon.model.entity.Server;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.demo.DemoNetwork;
import com.rehivetech.beeeon.network.server.Network;
import com.rehivetech.beeeon.persistence.DashBoardPersistence;
import com.rehivetech.beeeon.persistence.GraphSettingsPersistence;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.Utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Core of application (used as singleton), provides methods and access to all data and household.
 *
 * @author Robyer
 */
public final class Controller {

    public static final String TAG = Controller.class.getSimpleName();

    /**
     * This singleton instance.
     */
    private static Controller sController;

    /**
     * Switch for using demo mode (with example gate, without server)
     */
    private final boolean mDemoMode;

    /**
     * Application context
     */
    private final Context mContext;

    /**
     * Persistence service for caching purposes
     */
    private final Persistence mPersistence;

    /**
     * Network service for communication with server
     */
    private final INetwork mNetwork;

    /**
     * Active user object
     */
    private final User mUser;

    /**
     * Active gate
     */
    private Gate mActiveGate;

    /**
     * Models for keeping and handling data
     */
    private final Map<String, BaseModel> mModels = new HashMap<>();

    /**
     * Return singleton instance of this Controller. This is thread-safe.
     *
     * @param context any context, will be used application context
     * @return singleton instance of controller
     */
    @NonNull
    public static Controller getInstance(@NonNull Context context) {
        if (sController == null) {
            synchronized (Controller.class) {
                if (sController == null) {
                    // Create new singleton instance of controller
                    sController = new Controller(context);
                }
            }
        }

        return sController;
    }

    /**
     * Private constructor.
     *
     * @param context
     */
    private Controller(@NonNull Context context) {
        mContext = context.getApplicationContext();
        // Load last used mode
        mDemoMode = Persistence.loadLastDemoMode(mContext);
        // Create basic objects
        mNetwork = loadNetwork();

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
            // Finally load sessionId - we can call it directly like that because here we doesn't care whether it's empty because it's empty since Network creation
            mNetwork.setSessionId(mPersistence.loadLastBT(userId));
        }
    }

    public INetwork loadNetwork() throws AppException {
        // Load login server
        long serverId = Persistence.loadLoginServerId(mContext);

        // get login server
        Server server = Server.getServerSafeById(serverId);

        return mDemoMode ? new DemoNetwork() : new Network(server);
    }

    /**
     * Recreates the actual Controller object to use with different user or demo mode.
     * <p/>
     * This internally creates new instance of Controller with changed mode (e.g. demoMode or normal).
     * You MUST call getInstance() again to get fresh instance and DON'T remember or use the previous.
     *
     * @param context
     * @param demoMode
     */
    public static synchronized void setDemoMode(@NonNull Context context, boolean demoMode) {
        // Remember last used mode
        Persistence.saveLastDemoMode(context, demoMode);

        // We always need to create a new Controller, due to account switch and first (not) loading of demo
        sController = new Controller(context);
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
    public BaseModel getModelInstance(@NonNull Class<? extends BaseModel> modelClass) {
        final String name = modelClass.getName();

        if (!mModels.containsKey(name)) {
            synchronized (mModels) {
                if (!mModels.containsKey(name)) {
                    CacheHoldTime.Item cacheHoldTime = (CacheHoldTime.Item) new CacheHoldTime().fromSettings(getUserSettings());

                    // Known parameters we can automatically give to model constructor
                    final Object[] supportedParams = {mNetwork, mContext, mPersistence, mUser, cacheHoldTime};

                    // Create instance of the given model class
                    final Constructor constructor = modelClass.getConstructors()[0];
                    final List<Object> params = new ArrayList<>();
                    for (Class<?> pType : constructor.getParameterTypes()) {
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

    public GatesModel getGatesModel() {
        return (GatesModel) getModelInstance(GatesModel.class);
    }

    public LocationsModel getLocationsModel() {
        return (LocationsModel) getModelInstance(LocationsModel.class);
    }

    public DevicesModel getDevicesModel() {
        return (DevicesModel) getModelInstance(DevicesModel.class);
    }

    public UninitializedDevicesModel getUninitializedDevicesModel() {
        return (UninitializedDevicesModel) getModelInstance(UninitializedDevicesModel.class);
    }

    public ModuleLogsModel getModuleLogsModel() {
        return (ModuleLogsModel) getModelInstance(ModuleLogsModel.class);
    }

    public GcmModel getGcmModel() {
        return (GcmModel) getModelInstance(GcmModel.class);
    }

    public UsersModel getUsersModel() {
        return (UsersModel) getModelInstance(UsersModel.class);
    }

    public WeatherModel getWeatherModel() {
        return (WeatherModel) getModelInstance(WeatherModel.class);
    }

    /**
     * Persistence methods ************************************************
     */

    @Nullable
    public IAuthProvider getLastAuthProvider() {
        return mPersistence.loadLastAuthProvider();
    }

    /**
     * Get SharedPreferences for actually logged in user
     *
     * @return null if user is not logged in
     */
    @Nullable
    public SharedPreferences getUserSettings() {
        String userId = mUser.getId();
        if (userId.isEmpty()) {
            Log.e(TAG, "getUserSettings() with no loaded userId");
            return null;
        }

        return mPersistence.getSettings(userId);
    }

    /** Communication methods ***********************************************/

    /**
     * Load user data from server and save them to cache.
     *
     * @param userId can be null when this is first login
     */
    public void loadUserData(@Nullable String userId) {
        // Load cached user details, if this is not first login
        if (userId != null) {
            mPersistence.loadUserDetails(userId, mUser);
        }

        // Load user data from server
        User user = mNetwork.accounts_getMyProfile();

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
    public boolean login(@NonNull IAuthProvider authProvider) throws AppException {
        // In demo mode load some init data
        if (mNetwork instanceof DemoNetwork) {
            ((DemoNetwork) mNetwork).initDemoData();
        }

        // We don't have beeeon-token yet, try to login
        mNetwork.accounts_login(authProvider); // throws exception on error

        // Load user data so we will know our userId
        loadUserData(null);

        // Do we have session now?
        if (!mNetwork.hasSessionId()) {
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
            // Save our new sessionId
            String bt = mNetwork.getSessionId();
            Log.i(TAG, String.format("Loaded for user '%s' fresh new sessionId: %s", userId, bt));
            mPersistence.saveLastBT(userId, bt);

            // Remember this email to use with auto login
            mPersistence.saveLastUserId(mUser.getId());
            mPersistence.saveLastAuthProvider(authProvider);

            Intent intent = new Intent(mContext, GcmRegistrationIntentService.class);
            mContext.startService(intent);
        }

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
    public boolean register(@NonNull IAuthProvider authProvider) throws AppException {
        // We don't have beeeon-token yet, try to login
        return mNetwork.accounts_register(authProvider); // throws exception on error
    }

    /**
     * Destroy user session in network and forget him as last logged in user.
     *
     * @param alsoFromServer If set to true, it also send request to server to logout actual user. Otherwise only clear user data locally.
     */
    public void logout(boolean alsoFromServer) {
        if (alsoFromServer) {
            // TODO: Request to logout from server (discard actual sessionId)

            // Delete GCM id on server side
            getGcmModel().deleteGCM(mUser.getId(), null);
        }

        // delete all visible notification
        NotificationManager notifMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifMgr.cancelAll();

        // Destroy session
        mNetwork.setSessionId("");

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
        return mNetwork.hasSessionId();
    }

    /**
     * Return active gate.
     *
     * @return active gate, or first gate, or null if there are no gates
     */
    @Nullable
    public synchronized Gate getActiveGate() {
        if (mActiveGate == null) {
            // UserSettings can be null when user is not logged in!
            SharedPreferences prefs = getUserSettings();

            String lastId = (prefs == null) ? "" : prefs.getString(Constants.PERSISTENCE_PREF_ACTIVE_GATE, "");

            mActiveGate = getGatesModel().getGateOrFirst(lastId);

            if (mActiveGate != null && prefs != null)
                prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_GATE, mActiveGate.getId()).apply();
        }

        return mActiveGate;
    }

    /**
     * Sets active gate and load all locations and devices, if needed (or if forceReload = true)
     * <p/>
     * This CAN'T be called on UI thread!
     *
     * @param id
     * @param forceReload
     * @return true on success, false if there is no gate with this id
     */
    public synchronized boolean setActiveGate(String id, boolean forceReload) {
        // UserSettings can be null when user is not logged in!
        SharedPreferences prefs = getUserSettings();
        if (prefs != null) {
            // Save it whether gate below will be loaded or not
            prefs.edit().putString(Constants.PERSISTENCE_PREF_ACTIVE_GATE, id).apply();
        }

        // Find specified gate
        mActiveGate = getGatesModel().getGate(id);

        if (mActiveGate == null) {
            Log.d(TAG, String.format("Can't set active gate to '%s'", id));
            return false;
        }

        Log.d(TAG, String.format("Set active gate to '%s'", mActiveGate.getName()));

        // Load locations and devices, if needed
        getLocationsModel().reloadLocationsByGate(id, forceReload);
        getDevicesModel().reloadDevicesByGate(id, forceReload);

        return true;
    }

    @NonNull
    public User getActualUser() {
        return mUser;
    }

    /**
     * UAC
     */
    public boolean isUserAllowed(@NonNull Role role) {
        return !(role.equals(Role.User) || role.equals(Role.Guest));
    }

    /**
     * Interrupts actual connection (opened socket) of Network module.
     * <p/>
     * This CAN'T be called on UI thread!
     */
    public void interruptConnection() {
        if (mNetwork instanceof Network) {
            ((Network) mNetwork).interruptConnection();
        }
    }

    /**
     * Read dashboard items from preferences
     *
     * @param gateId active gate id
     * @return dashboard items list
     */
    public List<BaseItem> getDashboardItems(int index, String gateId) {
        String userId = getActualUser().getId();
        List<List<BaseItem>> items = DashBoardPersistence.load(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS);

        return items != null && items.size() > index ? items.get(index) : null;
    }

    public int getNumberOfDashboardTabs(String userId, String gateId) {
        return mPersistence.getSettings(getDashboardKey(userId, gateId)).getInt("items", 0);
    }

    public void saveNumberOfDashboardTabs(String userId, String gateId, int numOfItems) {
        mPersistence.getSettings(getDashboardKey(userId, gateId)).edit().putInt("items", numOfItems).apply();
    }

    /**
     * Save actual state of dashboard into preferences
     *
     * @param gateId active gate id
     * @param items  dashboard items
     */
    public void saveDashboardItems(int index, String gateId, List<BaseItem> items) {
        String userId = getActualUser().getId();
        List<List<BaseItem>> itemsList = DashBoardPersistence.load(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS);

        if (itemsList != null && itemsList.size() > index && itemsList.get(index) != null) {
            itemsList.get(index).clear();
            itemsList.get(index).addAll(items);

        } else if (itemsList != null) {
            itemsList.add(items);
        } else {
            itemsList = new ArrayList<>();
            itemsList.add(items);
        }

        DashBoardPersistence.save(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS, itemsList);
    }

    /**
     * Remove all dashboard cards by gate
     *
     * @param gateId id of gate
     */
    public void removeDashboardView(int index, String gateId) {
        String userId = getActualUser().getId();
        if (index > -1) {

            List<List<BaseItem>> itemsList = DashBoardPersistence.load(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS);

            if (itemsList != null && itemsList.size() > index && itemsList.get(index) != null) {
                itemsList.remove(index);
            }

            DashBoardPersistence.save(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS, itemsList);
        } else {

            SharedPreferences preferences = mPersistence.getSettings(getDashboardKey(userId, gateId));
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS);
            editor.putString(Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS, "");
            editor.apply();

            saveNumberOfDashboardTabs(userId, gateId, 0);
        }
    }

    /**
     * Remove all device occurrences in dashboard cards
     *
     * @param gateId          actual gate id
     * @param removedDeviceId id of removed device
     */
    public void removeDeviceFromDashboard(String gateId, String removedDeviceId) {
        String userId = getActualUser().getId();
        List<List<BaseItem>> items = DashBoardPersistence.load(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS);

        if (items == null) return; // we don't have any dashboard preferences

        ListIterator<List<BaseItem>> listIterator = items.listIterator();

        while (listIterator.hasNext()) {

            List<BaseItem> listItem = listIterator.next();
            Iterator<BaseItem> itemsIterator = listItem.iterator();

            while (itemsIterator.hasNext()) {
                BaseItem item = itemsIterator.next();

                if (item instanceof GraphItem) {

                    Iterator<String> moduleIdsIterator = ((GraphItem) item).getAbsoluteModuleIds().iterator();
                    while (moduleIdsIterator.hasNext()) {
                        String absoluteModuleId = moduleIdsIterator.next();
                        if (absoluteModuleId.startsWith(removedDeviceId)) {
                            moduleIdsIterator.remove();
                        }
                    }
                    if (((GraphItem) item).getAbsoluteModuleIds().size() == 0) {
                        itemsIterator.remove();

                    }

                } else if (item instanceof ActualValueItem) {
                    if (((ActualValueItem) item).getAbsoluteModuleId().startsWith(removedDeviceId)) {
                        itemsIterator.remove();
                    }

                } else if (item instanceof OverviewGraphItem) {
                    if (((OverviewGraphItem) item).getAbsoluteModuleId().startsWith(removedDeviceId)) {
                        itemsIterator.remove();
                    }
                }
            }

            if (listItem.isEmpty()) {
                listIterator.remove();
            }

        }

        DashBoardPersistence.save(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS, items);
    }

    public void migrateDashboard() {
        String userId = getActualUser().getId();
        List<Gate> gates = getGatesModel().getGates();

        for (Gate gate : gates) {
            String gateId = gate.getId();
            List<BaseItem> dashboardOld = DashBoardPersistence.loadOld(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS);

            if (dashboardOld != null) {
                List<List<BaseItem>> dashboardNew = new ArrayList<>();
                dashboardNew.add(dashboardOld);
                DashBoardPersistence.save(mPersistence.getSettings(getDashboardKey(userId, gateId)), Constants.PERSISTENCE_PREF_DASHBOARD_ITEMS, dashboardNew);
            }
        }
    }

    /**
     * Create helper instance of graph settings
     *
     * @param gateId           id of actual gate
     * @param absoluteModuleId absolute module id
     * @param graphRange       data range of graph
     * @return graphSettings instance
     */
    public GraphSettingsPersistence getGraphSettingsPersistence(String gateId, String absoluteModuleId, @ChartHelper.DataRange int graphRange) {
        String key = getGraphSettingsKey(gateId, absoluteModuleId, graphRange);
        SharedPreferences preferences = mPersistence.getSettings(key);

        return new GraphSettingsPersistence(preferences);
    }

    /**
     * Create graphSettings key
     *
     * @param gateId           id of actual gate
     * @param absoluteModuleId absolute module id
     * @param graphRange       data range of graph
     * @return key string
     */
    private String getGraphSettingsKey(String gateId, String absoluteModuleId, int graphRange) {
        return String.format("%s-%s-%d", gateId, absoluteModuleId, graphRange);
    }

    /**
     * Create dashboard key
     *
     * @param userId id of actual user
     * @param gateId id of active gate
     * @return key string
     */
    private String getDashboardKey(String userId, String gateId) {
        return String.format("%s-%s", userId, gateId);
    }
}