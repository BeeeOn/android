package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.NameIdentifierComparator;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class UsersModel extends BaseModel {

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final MultipleDataHolder<User> mUsers = new MultipleDataHolder<>(); // adapterId => user dataHolder

	public UsersModel(INetwork network) {
		super(network);
	}

	/**
	 * Return user from active adapter by id.
	 *
	 * @param id
	 * @return User if found, null otherwise.
	 */
	public User getUser(String adapterId, String id) {
		return mUsers.getObject(adapterId, id);
	}

	/**
	 * Return list of users from adapter.
	 *
	 * @return List of users (or empty list)
	 */
	public List<User> getUsersByAdapter(String adapterId) {
		List<User> users = mUsers.getObjects(adapterId);

		// Sort result users by name, id
		Collections.sort(users, new NameIdentifierComparator());

		return users;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadUsersByAdapter(String adapterId, boolean forceReload) {
		if (!forceReload && !mUsers.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mUsers.setObjects(adapterId, mNetwork.getAccounts(adapterId));
		mUsers.setLastUpdate(adapterId, DateTime.now());

		return true;
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
			mUsers.addObject(adapterId, user);
			return true;
		}

		return false;
	}

	/**
	 * Deletes user from adapter from server and from list of users.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param user
	 * @return
	 */
	public boolean deleteUser(String adapterId, User user) {
		if (mNetwork.deleteAccount(adapterId, user)) {
			// Location was deleted on server, remove it from map too
			mUsers.removeObject(adapterId, user.getId());
			return true;
		}

		return false;
	}

	/**
	 * Create and add new location to server and to list of locations.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param user
	 * @return Location on success, null otherwise
	 */
	public boolean addUser(String adapterId, User user) {
		if (mNetwork.addAccount(adapterId, user)) {
			// User was added to server, update it in map too
			mUsers.addObject(adapterId, user);
			return true;
		}

		return false;
	}

}
