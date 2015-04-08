package com.rehivetech.beeeon.network;

import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Device.SaveDevice;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.CustomViewPair;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.network.xml.action.ComplexAction;
import com.rehivetech.beeeon.network.xml.condition.Condition;
import com.rehivetech.beeeon.pair.LogDataPair;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public interface INetwork {

	/**
	 * Action of View messages
	 *
	 * @author ThinkDeep
	 *
	 */
	public enum NetworkAction {
		REMOVE("0"), //
		ADD("1");

		private final String mAction;

		private NetworkAction(String action) {
			mAction = action;
		}

		public String getValue() {
			return mAction;
		}

		public static NetworkAction fromValue(String value) {
			for (NetworkAction item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid NetworkAction value");
		}
	}

	/**
	 * Checks if Internet connection is available.
	 *
	 * @return true if available, false otherwise
	 */
	public boolean isAvailable();

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS//////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Return beeeon-token used for communication
	 *
	 * @return BT of actual user
	 */
	public String getBT();

	/**
	 * Set beeeon-token for communication
	 *
	 * @return
	 */
	public void setBT(String token);

	/**
	 * Check if beeeon-token is present (but does NOT check if it is still valid on server)
	 * @return
	 */
	public boolean hasBT();

	/**
	 * Download information about actual user from server
	 *
	 * @return User object with data from server
	 */
	public User loadUserInfo();

	/**
	 * Method log in user by specified provider
	 *
	 * @param authProvider provider object with data for authentication
	 * @return true if user has been logged in with this provider, false otherwise
	 */
	public boolean loginMe(IAuthProvider authProvider);

	/**
	 * Method register user to server by specified provider
	 *
	 * @param authProvider provider object with data for authentication
	 * @return true if user has beed added to database with this provider, false otherwise
	 */
	public boolean registerMe(IAuthProvider authProvider);

	/**
	 * Method add new provider information (join your accounts) to your account
	 * @param authProvider
	 * @return true if everything is ok, false otherwise
	 */
	public boolean addProvider(IAuthProvider authProvider);

	/**
	 * Method remove one of your provider from your account
	 * @param providerName
	 * @return
	 */
	public boolean removeProvider(String providerName);

	/**
	 * Method remove all providers, so remove whole account from system
	 * @return
	 */
	public boolean deleteMyAccount();

	/**
	 * Method register adapter to server
	 *
	 * @param adapterID
	 *            adapter id
	 * @param adapterName
	 *            adapter name
	 * @return true if adapter has been registered, false otherwise
	 */
	public boolean addAdapter(String adapterID, String adapterName);

	/**
	 * Method ask for list of adapters. User has to be sign in before
	 *
	 * @return list of adapters or empty list
	 */
	public List<Adapter> getAdapters();

	/**
	 * Method ask for whole adapter data
	 *
	 * @param adapterID
	 *            of wanted adapter
	 * @return Adapter
	 */
	public List<Facility> initAdapter(String adapterID);

	/**
	 * Method change adapter id
	 *
	 * @param oldId
	 *            id to be changed
	 * @param newId
	 *            new id
	 * @return true if change has been successfully
	 */
	public boolean reInitAdapter(String oldId, String newId);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send updated fields of devices
	 *
	 * @return true if everything goes well, false otherwise
	 */
	public boolean updateFacilities(String adapterID, List<Facility> facilities, EnumSet<SaveDevice> toSave);

	/**
	 * Method send wanted fields of device to server
	 *
	 * @param adapterID
	 *            id of adapter
	 * @param device
	 *            to save
	 * @param toSave
	 *            ENUMSET specified fields to save
	 * @return true if fields has been updated, false otherwise
	 */
	public boolean updateDevice(String adapterID, Device device, EnumSet<SaveDevice> toSave);

	/**
	 * Method toggle or set actor to new value
	 *
	 * @param adapterID
	 * @param device
	 * @return
	 */
	public boolean switchState(String adapterID, Device device);

	/**
	 * Method make adapter to special state, when listen for new sensors (e.g. 15s) and wait if some sensors has been
	 * shaken to connect
	 *
	 * @param adapterID
	 * @return
	 */
	public boolean prepareAdapterToListenNewSensors(String adapterID);

	/**
	 * Method delete facility from server
	 *
	 * @param adapterID
	 * @param facility
	 *            to be deleted
	 * @return true if is deleted, false otherwise
	 */
	public boolean deleteFacility(String adapterID, Facility facility);

	/**
	 * Method ask for actual data of facilities
	 *
	 * @param facilities
	 *            list of facilities to which needed actual data
	 * @return list of updated facilities fields
	 */
	public List<Facility> getFacilities(List<Facility> facilities);

	/**
	 * Method ask server for actual data of one facility
	 *
	 * @param facility
	 * @return
	 */
	public Facility getFacility(Facility facility);

	public boolean updateFacility(String adapterID, Facility facility, EnumSet<SaveDevice> toSave);

	/**
	 * TODO: need to test
	 *
	 * @param adapterID
	 * @return
	 */
	public List<Facility> getNewFacilities(String adapterID);

	/**
	 * Method ask for data of logs
	 *
	 * @param pair
	 *            data of log (from, to, type, interval)
	 * @return list of rows with logged data
	 */
	public DeviceLog getLog(String adapterID, Device device, LogDataPair pair);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ROOMS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method call to server for actual list of locations
	 *
	 * @return List with locations
	 */
	public List<Location> getLocations(String adapterID);

	/**
	 * Method call to server to update location
	 *
	 * @param locations
	 *            to update
	 * @return true if everything is OK, false otherwise
	 */
	public boolean updateLocations(String adapterID, List<Location> locations);

	/**
	 * Method call to server to update location
	 *
	 * @param location
	 * @return
	 */
	public boolean updateLocation(Location location);

	/**
	 * Method call to server and delete location
	 *
	 * @param location
	 * @return true room is deleted, false otherwise
	 */
	public boolean deleteLocation(Location location);

	public Location createLocation(Location location);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////VIEWS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send newly created custom view
	 *
	 * @param viewName
	 *            name of new custom view
	 * @param iconID
	 *            icon that is assigned to the new view
	 * @return true if everything goes well, false otherwise
	 */
	public boolean addView(String viewName, int iconID, List<Device> devices);

	/**
	 * Method ask for list of all custom views
	 *
	 * @return list of defined custom views
	 */
	public List<CustomViewPair> getViews();

	/**
	 * Method delete whole custom view from server
	 *
	 * @param viewName
	 *            name of view to erase
	 * @return true if view has been deleted, false otherwise
	 */
	public boolean deleteView(String viewName);

	public boolean updateView(String viewName, int iconId, Facility facility, NetworkAction action);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ACCOUNTS////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	public boolean addAccounts(String adapterID, ArrayList<User> users);

	/**
	 * Method add new user to adapter
	 *
	 * @param adapterID
	 * @return
	 */
	public boolean addAccount(String adapterID, User user);

	/**
	 * Method delete users from actual adapter
	 *
	 * @param users
	 *            email of user
	 * @return true if all users has been deleted, false otherwise
	 */
	public boolean deleteAccounts(String adapterID, List<User> users);

	/**
	 * Method delete on user from adapter
	 *
	 * @param adapterID
	 * @param user
	 * @return
	 */
	public boolean deleteAccount(String adapterID, User user);

	/**
	 * Method ask for list of users of current adapter
	 *
	 * @return Map of users where key is email and value is User object
	 */
	public ArrayList<User> getAccounts(String adapterID);

	/**
	 * Method update users roles on server on current adapter
	 *
	 *            map with email as key and role as value
	 * @return true if all accounts has been changed false otherwise
	 */
	public boolean updateAccounts(String adapterID, ArrayList<User> users);

	/**
	 * Method update users role on adapter
	 *
	 * @param adapterID
	 * @param user
	 * @return
	 */
	public boolean updateAccount(String adapterID, User user);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////TIME////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set wanted time zone to server
	 *
	 * @NOTE using difference from GMT (UTC+0),
	 *       https://merlin.fit.vutbr.cz/wiki-iot/index.php/Smarthome_cloud#SetTimeZone
	 * @param differenceToGMT
	 * @return
	 */
	public boolean setTimeZone(String adapterID, int differenceToGMT);

	/**
	 * Method call to server to get actual time zone
	 *
	 * @return integer in range <-12,12>
	 */
	public int getTimeZone(String adapterID);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////NOTIFICATIONS///////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set read flag to notification on server
	 *
	 * @param msgID
	 *            id of notification
	 * @return true if server took flag, false otherwise
	 */
	public boolean NotificationsRead(ArrayList<String> msgID);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////CONDITIONS,ACTIONS//////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	public Condition setCondition(Condition condition);

	public boolean connectConditionWithAction(String conditionID, String actionID);

	public Condition getCondition(Condition condition);

	public List<Condition> getConditions();

	public boolean updateCondition(Condition condition);

	public boolean deleteCondition(Condition condition);

	public ComplexAction setAction(ComplexAction action);

	public List<ComplexAction> getActions();

	public ComplexAction getAction(ComplexAction action);

	public boolean updateAction(ComplexAction action);

	public boolean deleteAction(ComplexAction action);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ALGORITHMS//////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	public boolean addWatchDog(WatchDog watchDog, String AdapterID);

	public ArrayList<WatchDog> getWatchDogs(ArrayList<String> watchDogIds, String adapterID);

	public ArrayList<WatchDog> getAllWatchDogs(String adapterID);

	public boolean updateWatchDog(WatchDog watchDog, String AdapterId);

	public boolean deleteWatchDog(WatchDog watchDog);

	public boolean passBorder(String regionId, String type);
}
