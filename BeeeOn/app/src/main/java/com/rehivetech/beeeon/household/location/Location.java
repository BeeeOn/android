package com.rehivetech.beeeon.household.location;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

public class Location implements INameIdentifier {
	/**
	 * Represents id of newly created location (not saved on server yet)
	 */
	public static final String NEW_LOCATION_ID = "-NEW-";

	/**
	 * Represents id of "no location" (server requires "null" string)
	 */
	public static final String NO_LOCATION_ID = "00000000-0000-0000-0000-000000000000";

	/**
	 * Represents type (= icon) of "no location"
	 */
	public static final String NO_LOCATION_TYPE = "0";

	protected final String mGateId;
	protected String mId = "";
	protected String mName = "";
	protected String mType;
	protected LocationIcon mIcon = LocationIcon.UNKNOWN;

	/**
	 * Represents location icon.
	 */
	public enum LocationIcon implements IIdentifier {
		UNKNOWN(0, R.drawable.ic_loc_unknown, R.drawable.ic_loc_unknown_gray),
		BATHROOM(1, R.drawable.ic_loc_bathroom, R.drawable.ic_loc_bathroom_gray),
		BEDROOM(2, R.drawable.ic_loc_bedroom, R.drawable.ic_loc_bedroom_gray),
		GARDEN(3, R.drawable.ic_loc_garden, R.drawable.ic_loc_garden_gray),
		DINING_ROOM(4, R.drawable.ic_loc_dining_room, R.drawable.ic_loc_dining_room_gray),
		LIVING_ROOM(5, R.drawable.ic_loc_living_room, R.drawable.ic_loc_living_room_gray),
		WC(6, R.drawable.ic_loc_wc, R.drawable.ic_loc_wc_gray);

		private final String mId;
		private final int mIconRes;
		private final int mIconResDark;

		LocationIcon(final int id, final int iconRes, final int iconResDark) {
			mId = String.valueOf(id);
			mIconRes = iconRes;
			mIconResDark = iconResDark;
		}

		public String getId() {
			return mId;
		}

		public int getIconResource(IconResourceType type) {
			return type == IconResourceType.WHITE ? mIconRes : mIconResDark;
		}
	}

	/**
	 * Represents default location.
	 */
	public enum DefaultLocation implements IIdentifier {
		BATHROOM(1, R.string.loc_bathroom),
		BEDROOM(2, R.string.loc_bedroom),
		GARDEN(3, R.string.loc_garden),
		DINING_ROOM(4, R.string.loc_dining_room),
		LIVING_ROOM(5, R.string.loc_living_room),
		WC(6, R.string.loc_wc);

		private final String mType;
		private final int mTitleRes;

		DefaultLocation(final int type, final int titleRes) {
			mType = String.valueOf(type);
			mTitleRes = titleRes;
		}

		public String getId() {
			return mType;
		}

		public int getTitleResource() {
			return mTitleRes;
		}
	}

	public Location(String id, String name, String gateId, String type) {
		setId(id);
		setName(name);
		setType(type);

		mGateId = gateId;
	}

	/**
	 * Get gate id of location
	 *
	 * @return gate id
	 */
	public String getGateId() {
		return mGateId;
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

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		mType = type;
		mIcon = Utils.getEnumFromId(LocationIcon.class, String.valueOf(type), LocationIcon.UNKNOWN);
	}

	public int getIconResource(IconResourceType type) {
		return mIcon.getIconResource(type);
	}

	public int getIconResource(){
		return getIconResource(IconResourceType.DARK);
	}

	@Override
	public String toString() {
		return getName();
	}
}
