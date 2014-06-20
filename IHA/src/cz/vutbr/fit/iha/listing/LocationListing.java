package cz.vutbr.fit.iha.listing;


public class LocationListing extends CustomizedListing {

	// protected Adapter mAdapter; // TODO: might this be useful here?
	
	private Location mLocation;

	public LocationListing(final String id) {
		super(id);
	}
	
	public LocationListing(final String id, final Location location) {
		super(id, location.getName());
		
		mLocation = location;
		mIcon = location.getType();
	}
	
	public Location getLocation(){
		return mLocation;
	}
	
}
