package cz.vutbr.fit.intelligenthomeanywhere.listing;

public class Location {
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
}
