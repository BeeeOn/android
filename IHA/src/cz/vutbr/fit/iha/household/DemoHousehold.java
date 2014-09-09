package cz.vutbr.fit.iha.household;

import java.util.ArrayList;

import android.content.Context;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.household.User.Gender;
import cz.vutbr.fit.iha.network.xml.XmlParsers;

/**
 * Represents demo household with adapters and facilities loaded from local assets files.
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
			XmlParsers parser = new XmlParsers();
			
			String assetName = Constants.ASSET_ADAPTERS_FILENAME;
			this.adapters = parser.getDemoAdaptersFromAsset(mContext, assetName);
			
			for (Adapter adapter : this.adapters) {
				assetName = String.format(Constants.ASSET_LOCATIONS_FILENAME, adapter.getId());
				adapter.setLocations(parser.getDemoLocationsFromAsset(mContext, assetName));
				
				assetName = String.format(Constants.ASSET_ADAPTER_DATA_FILENAME, adapter.getId());
				adapter.setFacilities(parser.getDemoFacilitiesFromAsset(mContext, assetName));
			}			
		}
		catch(Exception e){
			e.printStackTrace();
		}	
	}
	
}
