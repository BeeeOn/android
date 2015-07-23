package com.rehivetech.beeeon.util;

import com.rehivetech.beeeon.IIdentifier;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleDataHolder<O extends IIdentifier> {

	protected final Map<String, O> mObjects = new HashMap<>();

	/**
	 * Clear internal objects.
	 */
	public void clear() {
		mObjects.clear();
	}

	/**
	 * Return list with all objects.
	 *
	 * @return new List (ArrayList used here) with all objects added.
	 */
	public List<O> getObjects() {
		List<O> objects = new ArrayList<>();

		for (O obj : mObjects.values()) {
			objects.add(obj);
		}

		return objects;
	}

	/**
	 * Clear internal objects map and put all objects inside (identified by id).
	 *
	 * @param objects
	 */
	public void setObjects(List<O> objects) {
		clear();

		for (O obj : objects) {
			mObjects.put(obj.getId(), obj);
		}
	}

	/**
	 * Return object with specified id.
	 *
	 * @param id
	 * @return object with specified id or {@code null}, if no object was found.
	 */
	public O getObject(String id) {
		return mObjects.get(id);
	}

	/**
	 * Return object with specified id or first object, if specified one doesn't exists.
	 *
	 * @param id
	 * @return object with specified id, or first object, or {@code null}, if no object was found.
	 */
	public O getObjectOrFirst(String id) {
		// Try to return existing object
		if (mObjects.containsKey(id)) {
			return mObjects.get(id);
		}

		// Object with specified id wasn't found, let's return first one (if any exists)
		for (O obj : mObjects.values()) {
			return obj;
		}

		return null;
	}

	/**
	 * Add object to the map (identified by id).
	 *
	 * @param obj
	 * @return previous object in map with this id, or {@code null} if no object existed.
	 */
	public O addObject(O obj) {
		return mObjects.put(obj.getId(), obj);
	}

	/**
	 * Remove object from the map.
	 *
	 * @param id
	 * @return previous object in map with this id, or {@code null} if no object existed.
	 */
	public O removeObject(String id) {
		return mObjects.remove(id);
	}

	/**
	 * Checks if map is object with specified id.
	 *
	 * @param id
	 * @return true if object with this id in map exists, or false otherwise.
	 */
	public boolean hasObject(String id) {
		return mObjects.containsKey(id);
	}

}
