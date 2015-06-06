package com.rehivetech.beeeon.util;

import com.rehivetech.beeeon.IIdentifier;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extends SimpleDataHolder with ability to hold last update time of the data.
 */
public class DataHolder<O extends IIdentifier> extends SimpleDataHolder<O> {

	protected DateTime mLastUpdate;

	/**
	 * Clear internal objects and set last update to null.
	 */
	public void clear() {
		super.clear();
		mLastUpdate = null;
	}

	/**
	 * Set time of last update of this data.
	 *
	 * @param lastUpdate
	 */
	public void setLastUpdate(DateTime lastUpdate) {
		mLastUpdate = lastUpdate;
	}

	/**
	 * Check if this data was updated at least once.
	 *
	 * @return true if time of last update is not {@code null}, false otherwise.
	 */
	public boolean wasUpdated() {
		return mLastUpdate != null;
	}

	/**
	 * Check if this data are expired, depending on reload interval.
	 *
	 * @param reloadIntervalSeconds
	 * @return true if time of last update is {@code null} or more than specified number of seconds in past, false otherwise.
	 */
	public boolean isExpired(int reloadIntervalSeconds) {
		return mLastUpdate == null || mLastUpdate.plusSeconds(reloadIntervalSeconds).isBeforeNow();
	}

}
