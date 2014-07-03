package cz.vutbr.fit.iha.listing;



public abstract class CustomizedListing extends SimpleListing {

	protected final String mId;
	
	protected String mName;
	
	protected int mIcon = 0;
	
	public CustomizedListing(final String id) {
		mId = id;
	}
	
	public CustomizedListing(final String id, final String name) {
		this(id);

		mName = name;
	}
	
	public String getId() {
		return mId;
	}

	
	public void setName(final String name) {
		mName = name;
	}
	
	public String getName() {
		return mName;
	}

	
	public void setIcon(final int icon) {
		mIcon = icon;
	}
	
	public int getIcon() {
		return mIcon;
	}
	
	@Override
	public String toString() {
		return mName;
	}

}
