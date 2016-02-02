package com.rehivetech.beeeon.network;

import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.util.GpsData;

import java.util.ArrayList;
import java.util.List;

public interface INetwork {

	/**
	 * Checks if Internet connection is available.
	 *
	 * @return true if available, false otherwise
	 */
	boolean isAvailable();

	/**
	 * Return sessionId used for communication
	 *
	 * @return sessionId of actual user
	 */
	String getSessionId();

	/**
	 * Set sessionId for communication
	 *
	 * @param token
	 * @return
	 */
	void setSessionId(String token);

	/**
	 * Check if beeeon-token is present (but does NOT check if it is still valid on server)
	 *
	 * @return
	 */
	boolean hasSessionId();


	/**************************************************************************
	 * ACCOUNTS
	 */

	/**
	 * Method log in user by specified provider
	 *
	 * @param authProvider provider object with data for authentication
	 * @return true if user has been logged in with this provider, false otherwise
	 */
	boolean accounts_login(IAuthProvider authProvider);

	/**
	 * Method register user to server by specified provider
	 *
	 * @param authProvider provider object with data for authentication
	 * @return true if user has beed added to database with this provider, false otherwise
	 */
	boolean accounts_register(IAuthProvider authProvider);

	/**
	 * Logouts actual user, i.e. invalidate beeeon-token of actual user on server.
	 *
	 * @return true on success, false otherwise
	 */
	boolean accounts_logout();

	/**
	 * Download information about actual user from server.
	 *
	 * @return User object with data from server
	 */
	User accounts_getMyProfile();

	/**
	 * Delete whole user account from server.
	 *
	 * @return true on success, false otherwise
	 */
	// boolean accounts_deleteMyProfile();

	/**
	 * Update information about actual user on server
	 *
	 * @param user
	 * @return true on success, false otherwise
	 */
	// boolean accounts_updateMyProfile(User user);

	/**
	 * Method add new provider information (join your accounts) to your account
	 *
	 * @param authProvider
	 * @return true if everything is ok, false otherwise
	 */
	boolean accounts_connectAuthProvider(IAuthProvider authProvider);

	/**
	 * Method remove one of your provider from your account
	 *
	 * @param providerName
	 * @return
	 */
	boolean accounts_disconnectAuthProvider(String providerName);


	/**************************************************************************
	 * DEVICES
	 */

	/**
	 * Method get all devices of specified gate.
	 *
	 * @param gateId of wanted gate
	 * @return list of devices, or empty list
	 */
	List<Device> devices_getAll(String gateId);

	/**
	 * Method ask server for uninitialized (new) devices of specified gate.
	 *
	 * @param gateId
	 * @return list of devices, or empty list
	 */
	List<Device> devices_getNew(String gateId);

	/**
	 * Method ask for actual data of specified devices
	 *
	 * @param devices list of devices to which needed actual data
	 * @return list of updated devices fields
	 */
	List<Device> devices_get(List<Device> devices);

	/**
	 * Method ask server for actual data of one device
	 *
	 * @param device
	 * @return
	 */
	Device devices_get(Device device);

	/**
	 * Method ask for module  logs
	 *
	 * @param gateId
	 * @param module
	 * @param pair
	 * @return
	 */
	ModuleLog devices_getLog(String gateId, Module module, ModuleLog.DataPair pair);

	/**
	 * Update devices settings (like name or refresh interval).
	 *
	 * @param gateId
	 * @param devices
	 * @return true on success, false otherwise
	 */
	boolean devices_update(String gateId, List<Device> devices);

	/**
	 * Update device settings (like name or refresh interval).
	 *
	 * @param gateId
	 * @param device
	 * @return true on success, false otherwise
	 */
	boolean devices_update(String gateId, Device device);

	/**
	 * Method toggle or set actor to new value
	 *
	 * @param gateId
	 * @param module
	 * @return
	 */
	boolean devices_setState(String gateId, Module module);

	/**
	 * Method delete device from server
	 *
	 * @param device to be deleted
	 * @return true if is deleted, false otherwise
	 */
	boolean devices_unregister(Device device);


	/**************************************************************************
	 * GATES
	 */

	/**
	 * Method ask for list of gates. User has to be sign in before
	 *
	 * @return list of gates or empty list
	 */
	List<Gate> gates_getAll();

	/**
	 * Method ask for details about gate.
	 *
	 * @param gateId of wanted gate
	 * @return GateInfo
	 */
	GateInfo gates_get(String gateId);

	/**
	 * Method register gate to server
	 *
	 * @param gateId          gate id
	 * @param gateName        gate name
	 * @param offsetInMinutes
	 * @return true if gate has been registered, false otherwise
	 */
	boolean gates_register(String gateId, String gateName, int offsetInMinutes);

	/**
	 * Method unregister gate from this user. If this user is owner, removes gate and all data from whole server.
	 *
	 * @param gateId
	 * @return
	 */
	boolean gates_unregister(String gateId);

	/**
	 * Method make gate to special state, when listen for new sensors (e.g. 30s) and wait if some sensors has been
	 * shaken to connect
	 *
	 * @param gateId
	 * @return
	 */
	boolean gates_startListen(String gateId);

	/**
	 * Gate starts to search for new devices
	 *
	 * @param gateId
	 * @param deviceIpAddress
	 * @return
	 */
	boolean gates_search(String gateId, String deviceIpAddress);

	/**
	 * Method edits gate's name and timezone
	 *
	 * @param gate    new gate with new data
	 * @param gpsData gps data of this gate
	 * @return true if change was successful
	 */
	boolean gates_update(Gate gate, GpsData gpsData);

	/**
	 * Method opens ssh tunnel on the gate to allow direct connection from computer.
	 *
	 * @param gateId
	 * @return true on success, false otherwise
	 * @see #gates_get(String) to get address for connection (amongst other info)
	 */
	// boolean gates_openSsh(String gateId);


	/**************************************************************************
	 * GATEUSERS
	 */

	/**
	 * Method ask for list of users of current gate
	 *
	 * @param gateId
	 * @return list of users, or empty list
	 */
	List<User> gateusers_getAll(String gateId);

	/**
	 * Invite users to this gate. Logged in user must be owner of the gate.
	 *
	 * @param gateId
	 * @param users
	 * @return
	 */
	boolean gateusers_invite(String gateId, ArrayList<User> users);

	/**
	 * Invite user to this gate. Logged in user must be owner of the gate.
	 *
	 * @param gateId
	 * @param user
	 * @return
	 */
	boolean gateusers_invite(String gateId, User user);

	/**
	 * Method delete users access from gate.
	 *
	 * @param gateId
	 * @param users
	 * @return true if all users has been removed, false otherwise
	 */
	boolean gateusers_remove(String gateId, List<User> users);

	/**
	 * Method delete user access from gate.
	 *
	 * @param gateId
	 * @param user
	 * @return true if user has been removed, false otherwise
	 */
	boolean gateusers_remove(String gateId, User user);

	/**
	 * Method update users role for specified gate
	 *
	 * @param gateId
	 * @param users
	 * @return true if all accounts has been changed false otherwise
	 */
	boolean gateusers_updateAccess(String gateId, ArrayList<User> users);

	/**
	 * Method update user role for specified gate
	 *
	 * @param gateId
	 * @param user
	 * @return
	 */
	boolean gateusers_updateAccess(String gateId, User user);


	/**************************************************************************
	 * LOCATIONS
	 */

	/**
	 * Method create new location on server.
	 *
	 * @param location
	 * @return
	 */
	Location locations_create(Location location);

	/**
	 * Method update location settings on server.
	 *
	 * @param location
	 * @return true if location is updated, false otherwise
	 */
	boolean locations_update(Location location);

	/**
	 * Method call to server and delete location
	 *
	 * @param location
	 * @return true location is deleted, false otherwise
	 */
	boolean locations_delete(Location location);

	/**
	 * Method call to server for actual list of locations
	 *
	 * @param gateId
	 * @return List with locations, or empty list
	 */
	List<Location> locations_getAll(String gateId);


	/**************************************************************************
	 * NOTIFICATIONS
	 */

	// boolean notifications_registerService(NotificationProvider provider);

	// boolean notifications_unregisterService(String userId, NotificationProvider provider);

	// String notifications_userId();

	// String notifications_gcmId();

	List<VisibleNotification> notifications_getLatest(/* int count*/);

	boolean notifications_read(ArrayList<String> notificationIds);

	// boolean notifications_delete(ArrayList<String> notificationIds);

}
