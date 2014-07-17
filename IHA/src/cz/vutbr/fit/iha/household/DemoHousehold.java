package cz.vutbr.fit.iha.household;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.User;
import cz.vutbr.fit.iha.User.Gender;
import cz.vutbr.fit.iha.User.Role;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.adapter.parser.XmlParsers;

/**
 * Represents demo household with adapters and devices loaded from local assets files.
 * 
 * @author Robyer
 */
public final class DemoHousehold extends Household {
	
	private final Context mContext;
	
	public DemoHousehold(Context context) {
		mContext = context;

		prepareUser();
		prepareAdapters();
	}
	
	/**
	 * Prepare logged in user.
	 */
	private void prepareUser() {
		// TODO: role belongs to adapter so it has no meaning here
		this.user = new User("John Doe", "john@doe.com", Role.Superuser, Gender.Male);
	}
	
	/**
	 * Prepare demo adapters.
	 */
	private void prepareAdapters() {
		this.adapters = new ArrayList<Adapter>();
		
		try {
			Adapter adapter = XmlParsers.getDemoAdapterFromAsset(mContext, Constants.ASSET_ADAPTERS_FILENAME);
			List<Location> locations = XmlParsers.getDemoLocationsFromAsset(mContext, Constants.ASSET_LOCATIONS_FILENAME);
			adapter.setLocations(locations);
			
			this.adapters.add(adapter);
		}
		catch(Exception e){
			e.printStackTrace();
		}	
	}
	
}
