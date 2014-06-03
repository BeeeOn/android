package cz.vutbr.fit.intelligenthomeanywhere.household;

import java.util.ArrayList;

import android.content.Context;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.DemoData;
import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.User.Gender;
import cz.vutbr.fit.intelligenthomeanywhere.User.Role;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;
import cz.vutbr.fit.intelligenthomeanywhere.listing.FavoritesListing;

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
			//TODO: make new function
			Adapter adapter = XmlDeviceParser.fromFile(filename);
			this.adapters.add(adapter);
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
		list.setIcon(0);
		list.setDevices(this.adapters.get(0).getDevices());

		this.favoritesListings.add(list);
	}
	
}
