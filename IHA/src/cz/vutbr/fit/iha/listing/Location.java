package cz.vutbr.fit.iha.listing;

import cz.vutbr.fit.iha.R;

public class Location {
	/** Represents id of newly created location (not saved on server yet) */
	public static final String NEW_LOCATION_ID = "-NEW-";
	
	protected String mId = "";
	protected String mName = "";
	protected int mType;
	
	public static final int[] icons = {
		R.drawable.loc_unknown,		// 0
		R.drawable.loc_bath_room,	// 1	
		R.drawable.loc_bed_room,	// 2
		R.drawable.loc_garden,		// 3
		R.drawable.loc_dinner_room,	// 4
		R.drawable.loc_living_room,	// 5
		R.drawable.loc_wc,			// 6		
	};
	
	public Location() {}
	
	public Location(String id, String name, int type) {
		setId(id);
		setName(name);
		setType(type);
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
}
