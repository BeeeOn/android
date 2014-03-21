package cz.vutbr.fit.intelligenthomeanywhere.household;

import java.util.ArrayList;

import android.content.Context;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.User.Role;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;
import cz.vutbr.fit.intelligenthomeanywhere.listing.FavoritesListing;


public final class DemoHousehold extends Household {
	
	private final Context mContext;
	
	public DemoHousehold(Context context) {
		mContext = context;
		
		prepareUser();
		prepareAdapters();
		prepareListings();
	}
	
	private void prepareUser() {
		// Prepare demo user
		this.user = new User("John Doe", "john@doe.com", Role.Superuser);
	}
	
	private void prepareAdapters() {
		// Prepare demo adapters
		this.adapters = new ArrayList<Adapter>();
		
		String filename = mContext.getExternalFilesDir(null).getPath() + Constants.DEMO_FILENAME;
		Adapter adapter = XmlDeviceParser.fromFile(filename);

		this.adapters.add(adapter);
	}
	
	private void prepareListings() {
		// Prepare demo listings
		this.favoritesListings = new ArrayList<FavoritesListing>();
		
		FavoritesListing list = new FavoritesListing("demoFavorites");
		list.setName("My favorites");
		list.setIcon("favorites");
		
		// Add some devices to this listing
		BaseDevice[] devices = (BaseDevice[])this.adapters.get(0).devices.getDevices().values().toArray();
		
		list.addDevice(devices[3]);
		list.addDevice(devices[4]);
		list.addDevice(devices[7]);
		
		this.favoritesListings.add(list);
	}
	
}
