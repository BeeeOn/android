package com.rehivetech.beeeon.util;

import com.rehivetech.beeeon.IIdentifier;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipleDataHolder<O extends IIdentifier> {

	private final Map<String, DataHolder<O>> mHolders = new HashMap<>(); // holderId => (objectId => object)

	/**
	 * Clear underlaying holders.
	 */
	public void clear() {
		mHolders.clear();
	}

	/**
	 * Remove underlaying holder by id.
	 *
	 * @param holderId
	 * @return true when holder existed, false otherwise.
	 */
	public boolean removeHolder(String holderId) {
		return mHolders.remove(holderId) != null;
	}

	/**
	 * Helper method for returning holder by id, and if not exists, create new holder, put it to map and then return it.
	 *
	 * @param holderId
	 * @return DataHolder<O> object.
	 */
	private DataHolder<O> getOrCreateHolder(String holderId) {
		DataHolder<O> holder = mHolders.get(holderId);
		if (holder == null) {
			holder = new DataHolder<>();
			mHolders.put(holderId, holder);
		}

		return holder;
	}

	/**
	 * Return list with all objects from specified holder.
	 *
	 * @param holderId
	 * @return new List (ArrayList used here) with all objects added, or empty list.
	 * @see DataHolder#getObjects()
	 */
	public List<O> getObjects(String holderId) {
		DataHolder<O> holder = mHolders.get(holderId);
		if (holder == null) {
			return new ArrayList<O>();
		}

		return holder.getObjects();
	}

	/**
	 * Set objects of underlaying holder.
	 *
	 * @param holderId
	 * @param objects
	 * @see DataHolder#setObjects(java.util.List)
	 */
	public void setObjects(String holderId, List<O> objects) {
		DataHolder<O> holder = getOrCreateHolder(holderId);

		holder.setObjects(objects);
	}

	/**
	 * Get object from underlaying holder.
	 *
	 * @param holderId
	 * @param id
	 * @return object with specified id or {@code null}, if no object was found.
	 * @see DataHolder#getObject(String)
	 */
	public O getObject(String holderId, String id) {
		DataHolder<O> holder = mHolders.get(holderId);
		if (holder == null) {
			return null;
		}

		return holder.getObject(id);
	}

	/**
	 * Add object to underlaying holder.
	 *
	 * @param holderId
	 * @param obj
	 * @return previous object in map with this id, or {@code null} if no object existed.
	 * @see DataHolder#addObject(com.rehivetech.beeeon.IIdentifier)
	 */
	public O addObject(String holderId, O obj) {
		DataHolder<O> holder = getOrCreateHolder(holderId);

		return holder.addObject(obj);
	}

	/**
	 * Remove object from underlaying holder.
	 *
	 * @param holderId
	 * @param id
	 * @return previous object in map with this id, or {@code null} if no object existed.
	 * @see DataHolder#removeObject(String)
	 */
	public O removeObject(String holderId, String id) {
		DataHolder<O> holder = mHolders.get(holderId);
		if (holder == null) {
			return null;
		}

		return holder.removeObject(id);
	}

	/**
	 * Checks if underlaying holder has object with specified id.
	 *
	 * @param id
	 * @return true if object with this id in map exists, or false otherwise.
	 */
	public boolean hasObject(String holderId, String id) {
		DataHolder<O> holder = mHolders.get(holderId);
		if (holder == null) {
			return false;
		}

		return holder.hasObject(id);
	}

	/**
	 * Set last update of underlaying holder.
	 *
	 * @param holderId
	 * @param lastUpdate
	 * @see DataHolder#setLastUpdate(org.joda.time.DateTime)
	 */
	public void setLastUpdate(String holderId, DateTime lastUpdate) {
		DataHolder<O> holder = mHolders.get(holderId);
		if (holder == null) {
			return;
		}

		holder.setLastUpdate(lastUpdate);
	}

	/**
	 * Check if underlaying holder was updated at least once.
	 *
	 * @param holderId
	 * @return true if time of last update is not {@code null}, false otherwise.
	 * @see DataHolder#wasUpdated()
	 */
	public boolean wasUpdated(String holderId) {
		DataHolder<O> holder = mHolders.get(holderId);
		if (holder == null) {
			return false;
		}

		return holder.wasUpdated();
	}

	/**
	 * Check if underlaying holder is expired, depending on reload interval.
	 *
	 * @param holderId
	 * @param reloadIntervalSeconds
	 * @return true if time of last update is {@code null} or more than specified number of seconds in past, false otherwise.
	 * @see DataHolder#isExpired(int)
	 */
	public boolean isExpired(String holderId, int reloadIntervalSeconds) {
		DataHolder<O> holder = mHolders.get(holderId);
		if (holder == null) {
			return true;
		}

		return holder.isExpired(reloadIntervalSeconds);
	}

}
