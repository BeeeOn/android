package cz.vutbr.fit.iha.household;

import java.util.ArrayList;

import android.content.Context;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.household.User.Gender;
import cz.vutbr.fit.iha.network.xml.XmlParsers;

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
		user.setId("demo");
		user.setName("John Doe");
		user.setEmail("john@doe.com");
		user.setGender(Gender.Male);
	}
	
	/**
	 * Prepare demo adapters.
	 */
	private void prepareAdapters() {
		this.adapters = new ArrayList<Adapter>();
		
		try {
			Adapter adapter = new Adapter();
			adapter.setDevices(XmlParsers.getDemoDevicesFromAsset(mContext, Constants.ASSET_ADAPTERS_FILENAME));
					
			adapter.setId("01233145645792"); // original from xml
			adapter.setName("Home adapter");
			adapter.setLocations(XmlParsers.getDemoLocationsFromAsset(mContext, Constants.ASSET_LOCATIONS_FILENAME));
			this.adapters.add(adapter);

			adapter.setDevices(XmlParsers.getDemoDevicesFromAsset(mContext, Constants.ASSET_ADAPTERS_FILENAME));
			adapter.setId("16457841561538"); // random one
			adapter.setName("Testing duplicate");
			adapter.setLocations(XmlParsers.getDemoLocationsFromAsset(mContext, Constants.ASSET_LOCATIONS_FILENAME));
			this.adapters.add(adapter);
		}
		catch(Exception e){
			e.printStackTrace();
		}	
	}
	
}
