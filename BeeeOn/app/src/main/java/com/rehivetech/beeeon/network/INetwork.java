package com.rehivetech.beeeon.network;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public interface INetwork {

	/**
	 * Action of View messages
	 *
	 * @author ThinkDeep
	 */
	enum NetworkAction implements IIdentifier {
		REMOVE("0"), //
		ADD("1");

		private final String mAction;

		NetworkAction(String action) {
			mAction = action;
		}

		public String getId() {
			return mAction;
		}
	}

	/**
	 * Checks if Internet connection is available.
	 *
	 * @return true if available, false otherwise
	 */
	boolean isAvailable();

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////SIGNIN,SIGNUP,GATES//////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Return beeeon-token used for communication
	 *
	 * @return BT of actual user
	 */
	String getBT();

	/**
	 * Set beeeon-token for communication
	 *
	 * @return
	 */
	void setBT(String token);

	/**
	 * Check if beeeon-token is present (but does NOT check if it is still valid on server)
	 *
	 * @return
	 */
	boolean hasBT();

	/**
	 * Download information about actual user from server
	 *
	 * @return User object with data from server
	 */
	User loadUserInfo();

	/**
	 * Method log in user by specified provider
	 *
	 * @param authProvider provider object with data for authentication
	 * @return true if user has been logged in with this provider, false otherwise
	 */
	boolean loginMe(IAuthProvider authProvider);

	/**
	 * Method register user to server by specified provider
	 *
	 * @param authProvider provider object with data for authentication
	 * @return true if user has beed added to database with this provider, false otherwise
	 */
	boolean registerMe(IAuthProvider authProvider);

	/**
	 * Method add new provider information (join your accounts) to your account
	 *
	 * @param authProvider
	 * @return true if everything is ok, false otherwise
	 */
	boolean addProvider(IAuthProvider authProvider);

	/**
	 * Method remove one of your provider from your account
	 *
	 * @param providerName
	 * @return
	 */
	boolean removeProvider(String providerName);

	/**
	 * Method remove all providers, so remove whole account from system
	 *
	 * @return
	 */
	boolean deleteMyAccount();

	/**
	 * Method register gate to server
	 *
	 * @param gateID   gate id
	 * @param gateName gate name
	 * @return true if gate has been registered, false otherwise
	 */
	boolean addGate(String gateID, String gateName);

	/**
	 * Method ask for list of gates. User has to be sign in before
	 *
	 * @return list of gates or empty list
	 */
	List<Gate> getGates();

	/**
	 * Method ask for whole gate data
	 *
	 * @param gateID of wanted gate
	 * @return Gate
	 */
	List<Device> initGate(String gateID);

	/**
	 * Method change gate id
	 *
	 * @param oldId id to be changed
	 * @param newId new id
	 * @return true if change has been successfully
	 */
	boolean reInitGate(String oldId, String newId);

	/**
	 * Method edits gate's name and timezone
	 * @param gate new gate with new data
	 * @return true if change was successful
	 */
	boolean updateGate(Gate gate);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send updated fields of devices
	 *
	 * @return true if everything goes well, false otherwise
	 */
	boolean updateDevices(String gateID, List<Device> devices, EnumSet<SaveModule> toSave);

	/**
	 * Method send wanted fields of module to server
	 *
	 * @param gateID id of gate
	 * @param module to save
	 * @param toSave ENUMSET specified fields to save
	 * @return true if fields has been updated, false otherwise
	 */
	boolean updateModule(String gateID, Module module, EnumSet<SaveModule> toSave);

	/**
	 * Method toggle or set actor to new value
	 *
	 * @param gateID
	 * @param module
	 * @return
	 */
	boolean switchState(String gateID, Module module);

	/**
	 * Method make gate to special state, when listen for new sensors (e.g. 15s) and wait if some sensors has been
	 * shaken to connect
	 *
	 * @param gateID
	 * @return
	 */
	boolean prepareGateToListenNewSensors(String gateID);

	/**
	 * Method delete mDevice from server
	 *
	 * @param device to be deleted
	 * @return true if is deleted, false otherwise
	 */
	boolean deleteDevice(Device device);

	/**
	 * Method ask for actual data of devices
	 *
	 * @param devices list of devices to which needed actual data
	 * @return list of updated devices fields
	 */
	List<Device> getDevices(List<Device> devices);

	/**
	 * Method ask server for actual data of one mDevice
	 *
	 * @param device
	 * @return
	 */
	Device getDevice(Device device);

	boolean updateDevice(String gateID, Device device, EnumSet<SaveModule> toSave);

	/**
	 * TODO: need to test
	 *
	 * @param gateID
	 * @return
	 */
	List<Device> getNewDevices(String gateID);

	/**
	 * Method ask for data of logs
	 *
	 * @param pair data of log (from, to, type, interval)
	 * @return list of rows with logged data
	 */
	ModuleLog getLog(String gateID, Module module, ModuleLog.DataPair pair);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ROOMS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method call to server for actual list of locations
	 *
	 * @return List with locations
	 */
	List<Location> getLocations(String gateID);

	/**
	 * Method call to server to update location
	 *
	 * @param locations to update
	 * @return true if everything is OK, false otherwise
	 */
	boolean updateLocations(String gateID, List<Location> locations);

	/**
	 * Method call to server to update location
	 *
	 * @param location
	 * @return
	 */
	boolean updateLocation(Location location);

	/**
	 * Method call to server and delete location
	 *
	 * @param location
	 * @return true room is deleted, false otherwise
	 */
	boolean deleteLocation(Location location);

	Location createLocation(Location location);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ACCOUNTS////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	boolean addAccounts(String gateID, ArrayList<User> users);

	/**
	 * Method add new user to gate
	 *
	 * @param gateID
	 * @return
	 */
	boolean addAccount(String gateID, User user);

	/**
	 * Method delete users from actual gate
	 *
	 * @param users email of user
	 * @return true if all users has been deleted, false otherwise
	 */
	boolean deleteAccounts(String gateID, List<User> users);

	/**
	 * Method delete on user from gate
	 *
	 * @param gateID
	 * @param user
	 * @return
	 */
	boolean deleteAccount(String gateID, User user);

	/**
	 * Method ask for list of users of current gate
	 *
	 * @return Map of users where key is email and value is User object
	 */
	List<User> getAccounts(String gateID);

	/**
	 * Method update users roles on server on current gate
	 * <p/>
	 * map with email as key and role as value
	 *
	 * @return true if all accounts has been changed false otherwise
	 */
	boolean updateAccounts(String gateID, ArrayList<User> users);

	/**
	 * Method update users role on gate
	 *
	 * @param gateID
	 * @param user
	 * @return
	 */
	boolean updateAccount(String gateID, User user);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////TIME////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set wanted time zone to server
	 *
	 * @param offsetInMinutes - difference from GMT (UTC+0)
	 * @return
	 */
	boolean setTimeZone(String gateID, int offsetInMinutes);

	/**
	 * Method call to server to get actual time zone
	 *
	 * @return integer in range <-12,12>
	 */
	int getTimeZone(String gateID);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////NOTIFICATIONS///////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set read flag to notification on server
	 *
	 * @param msgID id of notification
	 * @return true if server took flag, false otherwise
	 */
	boolean NotificationsRead(ArrayList<String> msgID);

	List<VisibleNotification> getNotifications();

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ALGORITHMS//////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	boolean addWatchdog(Watchdog watchdog, String gateId);

	List<Watchdog> getWatchdogs(ArrayList<String> watchdogIds, String gateId);

	List<Watchdog> getAllWatchdogs(String gateId);

	boolean updateWatchdog(Watchdog watchdog, String gateId);

	boolean deleteWatchdog(Watchdog watchdog);

	boolean passBorder(String regionId, String type);
}
