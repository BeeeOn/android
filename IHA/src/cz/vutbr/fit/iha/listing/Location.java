package cz.vutbr.fit.iha.listing;

public class Location {
	/** Represents id of newly created location (not saved on server yet) */
	public static final String NEW_LOCATION_ID = "-NEW-";
	
	protected String mId = "";
	protected String mName = "";
	protected int mType;
	
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
	
	@Override
	public String toString() {
		return getName();
	}
}
