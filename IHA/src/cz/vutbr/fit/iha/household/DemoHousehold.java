package cz.vutbr.fit.iha.household;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.DemoData;
import cz.vutbr.fit.iha.User;
import cz.vutbr.fit.iha.User.Gender;
import cz.vutbr.fit.iha.User.Role;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.parser.XmlParsers;
import cz.vutbr.fit.iha.listing.CustomizedListing;
import cz.vutbr.fit.iha.listing.FavoritesListing;
import cz.vutbr.fit.iha.listing.Location;

/**
 * Represents demo household with adapters and devices loaded from local assets file.
 * 
 * @author Robyer
 */
public final class DemoHousehold extends Household {
	
	private final Context mContext;
	
	/**
	 * Constructor.
	 * @param context
	 * @throws Exception 
	 */
	public DemoHousehold(Context context) throws Exception {
		mContext = context;
		
		if(!(new DemoData(mContext)).checkDemoData()){
			throw new Exception("Something wrong with demo data");
		}
		
		prepareUser();
		prepareAdapters();
		prepareListings();
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
		try{
			String filename = mContext.getExternalFilesDir(null).getPath() + "/" +  Constants.DEMO_FILENAME;
			Adapter adapter = XmlParsers.getDemoAdapterFromFile(filename);
//			if(adapter == null){
//				if((new DemoData(mContext)).checkDemoData()){
//					//FIXME: do it better
//					adapter = XmlParsers.getDemoAdapterFromFile(filename);
//				}
//			}
			this.adapters.add(adapter);
			
			filename = mContext.getExternalFilesDir(null).getPath() + "/" +  Constants.DEMO_LOCATION_FILENAME;
			//TODO: do something with locations
			ArrayList<Location> lokace = XmlParsers.getDemoLocationsFromFile(filename);
			
			//FIXME: this is temporary solution (maybe getDemoLocationsFromFile should returns hashmap with id key)
			for(BaseDevice dev : adapter.getDevices()){
				for(Location loc : lokace){
					if(dev.getLocation().getId().equals(loc.getId())){
						dev.setLocation(loc);
						break;
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Prepare demo custom lists.
	 */
	private void prepareListings() {
		this.favoritesListings = new ArrayList<FavoritesListing>();
		
		FavoritesListing list = new FavoritesListing("demoFavorites");
		list.setName("My favorites");
		list.setIcon((new Random()).nextInt(CustomizedListing.icons.length));
		list.setDevices(this.adapters.get(0).getDevices());

		this.favoritesListings.add(list);
	}
	
}
