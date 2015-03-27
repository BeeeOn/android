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
	protected LocationIcon mIcon = LocationIcon.UNKNOWN;

	/** Represents location icon. */
	public static enum LocationIcon {
		UNKNOWN(0, R.drawable.loc_unknown),
		BATHROOM(1, R.drawable.loc_bath_room),
		BEDROOM(2, R.drawable.loc_bed_room),
		GARDEN(3, R.drawable.loc_garden),
		DINING_ROOM(4, R.drawable.loc_dinner_room),
		LIVING_ROOM(5, R.drawable.loc_living_room),
		WC(6, R.drawable.loc_wc);

		private final int mId;
		private final int mIconRes;

		private LocationIcon(final int id, final int iconRes) {
			mId = id;
			mIconRes = iconRes;
		}

		public int getId() {
			return mId;
		}

		public int getIconResource() {
			return mIconRes;
		}

		public static LocationIcon fromValue(int value) {
			for (LocationIcon item : values()) {
				if (value == item.getId())
					return item;
			}
			return LocationIcon.UNKNOWN;
		}
	}

	/**
	 * Represents default location.
	 */
	public static enum DefaultLocation {
		BATHROOM(1, R.string.loc_bathroom),
		BEDROOM(2, R.string.loc_bedroom),
		GARDEN(3, R.string.loc_garden),
		DINING_ROOM(4, R.string.loc_dining_room),
		LIVING_ROOM(5, R.string.loc_living_room),
		WC(6, R.string.loc_wc);

		private final int mType;
		private final int mTitleRes;

		private DefaultLocation(final int type, final int titleRes) {
			mType = type;
			mTitleRes = titleRes;
		}

		public int getType() {
			return mType;
		}

		public int getTitleResource() {
			return mTitleRes;
		}
	}

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
		mIcon = LocationIcon.fromValue(type);
	}

	public int getIconResource() {
		return mIcon.getIconResource();
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
