package com.rehivetech.beeeon.adapter.location;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;

public class Location implements IIdentifier, Comparable<Location> {
	/** Represents id of newly created location (not saved on server yet) */
	public static final String NEW_LOCATION_ID = "-NEW-";

	/** Represents id of "no location" (on server it is represented as empty id) */
	public static final String NO_LOCATION_ID = "";

	/** Represents type (= icon) of "no location" */
	public static final int NO_LOCATION_TYPE = 0;

	protected String mAdapterId;
	protected String mId = "";
	protected String mName = "";
	protected int mType;

	public static final int[] icons = { R.drawable.loc_unknown, // 0
			R.drawable.loc_bath_room, // 1
			R.drawable.loc_bed_room, // 2
			R.drawable.loc_garden, // 3
			R.drawable.loc_dinner_room, // 4
			R.drawable.loc_living_room, // 5
			R.drawable.loc_wc, // 6
	};

	/**
	 * Represents single default room.
	 */
	public static final class DefaultRoom {
		public final int type; // Icon type
		public final int rName; // Name resource

		public DefaultRoom(final int type, final int rName) {
			this.type = type;
			this.rName = rName;
		}
	}

	/**
	 * List of default rooms - icons and their names.
	 */
	public static DefaultRoom defaults[] = { new DefaultRoom(1, R.string.loc_bathroom), // 1
			new DefaultRoom(2, R.string.loc_bedroom), // 2
			new DefaultRoom(3, R.string.loc_garden), // 3
			new DefaultRoom(4, R.string.loc_dining_room), // 4
			new DefaultRoom(5, R.string.loc_living_room), // 5
			new DefaultRoom(6, R.string.loc_wc), // 6
	};

	public Location() {
	}

	public Location(String id, String name, int type) {
		setId(id);
		setName(name);
		setType(type);
	}

	/**
	 * Get adapter id of location
	 * 
	 * @return adapter id
	 */
	public String getAdapterId() {
		return mAdapterId;
	}

	/**
	 * Set adapter id of location
	 * 
	 * @param adapterId
	 */
	public void setAdapterId(String adapterId) {
		mAdapterId = adapterId;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getName() {
		return mName.length() > 0 ? mName : getId();
	}

	public void setName(String name) {
		mName = name;
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}

	public int getIconResource() {
		if (mType < 0 || mType >= icons.length)
			return icons[0];
		else
			return icons[mType];
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Location another) {
		return getName().compareTo(another.getName());
	}
}
