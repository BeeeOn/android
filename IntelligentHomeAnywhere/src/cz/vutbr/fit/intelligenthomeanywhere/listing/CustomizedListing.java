package cz.vutbr.fit.intelligenthomeanywhere.listing;


public abstract class CustomizedListing extends SimpleListing {

	private final String mId;
	
	private String mName;
	
	private String mIcon; // FIXME: use better type
	
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

	
	public void setIcon(final String icon) {
		mIcon = icon;
	}
	
	public String getIcon() {
		return mIcon;
	}
	
	@Override
	public String toString() {
		return mName;
	}

}
