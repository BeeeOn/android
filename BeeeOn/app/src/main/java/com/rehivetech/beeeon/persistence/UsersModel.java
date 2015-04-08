package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersModel {

	private final INetwork mNetwork;

	private final Map<String, Map<String, User>> mUsers = new HashMap<String, Map<String, User>>(); // adapterId => (userId => user)
	private final Map<String, DateTime> mLastUpdates = new HashMap<String, DateTime>(); // adapterId => lastUpdate of users

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	public UsersModel(INetwork network) {
		mNetwork = network;
	}

	/**
	 * Return user from active adapter by id.
	 *
	 * @param id
	 * @return User if found, null otherwise.
	 */
	public User getUser(String adapterId, String id) {
		Map<String, User> adapterUsers = mUsers.get(adapterId);
		if (adapterUsers == null) {
			return null;
		}

		return adapterUsers.get(id);
	}

	/**
	 * Return list of users from adapter.
	 *
	 * @return List of users (or empty list)
	 */
	public List<User> getUsersByAdapter(String adapterId) {
		List<User> users = new ArrayList<User>();

		Map<String, User> adapterUsers = mUsers.get(adapterId);
		if (adapterUsers != null) {
			for (User user : adapterUsers.values()) {
				users.add(user);
			}
		}

		// Sort result users by name+id
		Collections.sort(users, new NameIdentifierComparator());

		return users;
	}

	private void setUsersByAdapter(String adapterId, List<User> users) {
		Map<String, User> adapterUsers = mUsers.get(adapterId);
		if (adapterUsers != null) {
			adapterUsers.clear();
		} else {
			adapterUsers = new HashMap<String, User>();
			mUsers.put(adapterId, adapterUsers);
		}

		for (User user : users) {
			adapterUsers.put(user.getId(), user);
		}
	}

	private void setLastUpdate(String adapterId, DateTime lastUpdate) {
		mLastUpdates.put(adapterId, lastUpdate);
	}

	private boolean isExpired(String adapterId) {
		DateTime lastUpdate = mLastUpdates.get(adapterId);
		return lastUpdate == null || lastUpdate.plusSeconds(RELOAD_EVERY_SECONDS).isBeforeNow();
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadUsersByAdapter(String adapterId, boolean forceReload) {
		if (!forceReload && !isExpired(adapterId)) {
			return false;
		}

		// Don't check availability as we don't have cache working, so let Network notify connection error eventually
		return loadFromServer(adapterId);

		/*if (mNetwork.isAvailable()) {
			return loadFromServer(adapterId);
		} else if (forceReload) {
			return loadFromCache(adapterId);
		}

		return false;*/
	}

	private boolean loadFromServer(String adapterId) throws AppException {
		setUsersByAdapter(adapterId, mNetwork.getAccounts(adapterId));
		setLastUpdate(adapterId, DateTime.now());
		saveToCache(adapterId);

		return true;
	}

	private boolean loadFromCache(String adapterId) {
		// TODO: implement this
		return false;

		// setUsersByAdapter(usersFromCache);
		// setLastUpdate(adapterId, lastUpdateFromCache);
	}

	private void saveToCache(String adapterId) {
		// TODO: implement this
	}

	private void updateUserInMap(String adapterId, User user) {
		Map<String, User> adapterUsers = mUsers.get(adapterId);
		if (adapterUsers == null) {
			adapterUsers = new HashMap<String, User>();
			mUsers.put(adapterId, adapterUsers);
		}

		adapterUsers.put(adapterId, user);
	}

	/**
	 * Save changed user role to server and update it in list of users.
	 *
	 * @param adapterId
	 * @param user
	 * @return
	 */
	public boolean updateUser(String adapterId, User user) {
		if (mNetwork.updateAccount(adapterId, user)) {
			// User was updated on server, update it in map too
			updateUserInMap(adapterId, user);
			return true;
		}

		return false;
	}

	/**
	 * Deletes user from adapter from server and from list of users.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param user
	 * @return
	 */
	public boolean deleteUser(String adapterId, User user) {
		if (mNetwork.deleteAccount(adapterId, user)) {
			// Location was deleted on server, remove it from map too
			Map<String, User> adapterUsers = mUsers.get(adapterId);
			if (adapterUsers != null)
				adapterUsers.remove(user.getId());
			return true;
		}

		return false;
	}

	/**
	 * Create and add new location to server and to list of locations.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param user
	 * @return Location on success, null otherwise
	 */
	public boolean addUser(String adapterId, User user) {
		if (mNetwork.addAccount(adapterId, user)) {
			// User was added to server, update it in map too
			updateUserInMap(adapterId, user);
			return true;
		}

		return false;
	}

}
