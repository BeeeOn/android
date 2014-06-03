package cz.vutbr.fit.intelligenthomeanywhere.listing;


public class LocationListing extends CustomizedListing {

	// protected Adapter mAdapter; // TODO: might this be useful here?

	public LocationListing(final String id) {
		super(id);
	}
	
	public LocationListing(final String id, final Location location) {
		super(id, location.getName());
		
		mIcon = location.getType();
	}
	
}
