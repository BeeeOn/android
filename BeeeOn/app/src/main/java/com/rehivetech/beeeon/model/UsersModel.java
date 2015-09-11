package com.rehivetech.beeeon.model;

import android.content.SharedPreferences;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class UsersModel extends BaseModel {

	private static int RELOAD_EVERY_SECONDS = 10 * 60;

	private final MultipleDataHolder<User> mUsers = new MultipleDataHolder<>(); // gateId => user dataHolder

	public UsersModel(INetwork network, SharedPreferences prefs) {
		super(network);
		RELOAD_EVERY_SECONDS = Integer.parseInt(prefs.getString(CacheHoldTime.PERSISTENCE_CACHE_KEY, "0"));
	}

	/**
	 * Return user from active gate by id.
	 *
	 * @param id
	 * @return User if found, null otherwise.
	 */
	public User getUser(String gateId, String id) {
		return mUsers.getObject(gateId, id);
	}

	/**
	 * Return list of users from gate.
	 *
	 * @return List of users (or empty list)
	 */
	public List<User> getUsersByGate(String gateId) {
		List<User> users = mUsers.getObjects(gateId);

		// Sort result users by name, id
		Collections.sort(users, new NameIdentifierComparator());

		return users;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadUsersByGate(String gateId, boolean forceReload) {
		if (!forceReload && !mUsers.isExpired(gateId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mUsers.setObjects(gateId, mNetwork.getAccounts(gateId));
		mUsers.setLastUpdate(gateId, DateTime.now());

		return true;
	}

	/**
	 * Save changed user role to server and update it in list of users.
	 *
	 * @param gateId
	 * @param user
	 * @return
	 */
	public boolean updateUser(String gateId, User user) {
		if (mNetwork.updateAccount(gateId, user)) {
			// User was updated on server, update it in map too
			mUsers.addObject(gateId, user);
			return true;
		}

		return false;
	}

	/**
	 * Deletes user from gate from server and from list of users.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param user
	 * @return
	 */
	public boolean deleteUser(String gateId, User user) {
		if (mNetwork.deleteAccount(gateId, user)) {
			// Location was deleted on server, remove it from map too
			mUsers.removeObject(gateId, user.getId());
			return true;
		}

		return false;
	}

	/**
	 * Create and add new location to server and to list of locations.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param gateId
	 * @param user
	 * @return Location on success, null otherwise
	 */
	public boolean addUser(String gateId, User user) {
		if (mNetwork.addAccount(gateId, user)) {
			// User was added to server, update it in map too
			mUsers.addObject(gateId, user);
			return true;
		}

		return false;
	}

}
