package cz.vutbr.fit.iha.listing;

import cz.vutbr.fit.iha.R;


public abstract class CustomizedListing extends SimpleListing {

	protected final String mId;
	
	protected String mName;
	
	protected int mIcon = 0;
	
	public static final int[] icons = {
		R.drawable.loc_unknown,		// 0
		R.drawable.loc_bath_room,	// 1	
		R.drawable.loc_bed_room,	// 2
		R.drawable.loc_garden,		// 3
		R.drawable.loc_dinner_room,	// 4
		R.drawable.loc_living_room,	// 5
		R.drawable.loc_wc,			// 6		
	};
	
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
		if (mIcon < 0 || mIcon >= icons.length)
			return icons[0];
		else
			return icons[mIcon];
	}
	
	@Override
	public String toString() {
		return mName;
	}

}
