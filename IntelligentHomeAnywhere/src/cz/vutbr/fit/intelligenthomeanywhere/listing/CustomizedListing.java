package cz.vutbr.fit.intelligenthomeanywhere.listing;

import cz.vutbr.fit.intelligenthomeanywhere.R;


public abstract class CustomizedListing extends SimpleListing {

	protected final String mId;
	
	protected String mName;
	
	protected int mIcon;
	
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
	
	public int getIconResource() {
		// TODO: rewrite better
			
		switch (mIcon) {
		case 0: 
			// Koupelna
			return R.drawable.loc_bath_room;
		case 1: 
			// Loznice
			return R.drawable.loc_bed_room;
		case 2:
			// Jidelna
			return R.drawable.loc_dinner_room;
		case 3:
			// Zahrada
			return R.drawable.loc_garden;
		case 4:
			// Obyvak
			return R.drawable.loc_living_room;
		case 5:
			// WC
			return R.drawable.loc_wc;
		}
			
		return 0;
	}
	
	@Override
	public String toString() {
		return mName;
	}

}
