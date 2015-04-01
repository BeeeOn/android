package com.rehivetech.beeeon.network;

import android.content.Context;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Device.SaveDevice;
import com.rehivetech.beeeon.adapter.device.DeviceLog;
import com.rehivetech.beeeon.adapter.device.DeviceLog.DataInterval;
import com.rehivetech.beeeon.adapter.device.DeviceLog.DataType;
import com.rehivetech.beeeon.adapter.device.DeviceType;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.device.RefreshInterval;
import com.rehivetech.beeeon.adapter.device.values.BaseEnumValue;
import com.rehivetech.beeeon.adapter.device.values.BaseEnumValue.Item;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.User;
import com.rehivetech.beeeon.household.User.Gender;
import com.rehivetech.beeeon.household.User.Role;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.CustomViewPair;
import com.rehivetech.beeeon.adapter.watchdog.WatchDog;
import com.rehivetech.beeeon.network.xml.XmlParsers;
import com.rehivetech.beeeon.network.xml.action.ComplexAction;
import com.rehivetech.beeeon.network.xml.condition.Condition;
import com.rehivetech.beeeon.pair.LogDataPair;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Network service that handles communication in demo mode.
 * 
 * @author Robyer
 */
public class DemoNetwork implements INetwork {
	private static final String TAG = DemoNetwork.class.getSimpleName();

	public static final String DEMO_USER_ID = "demo";
	private static final String DEMO_USER_BT = "12345";

	private Context mContext;
	private User mUser;
	private String mBT;

	private class AdapterHolder {
		public final Adapter adapter;
		public final Map<String, Location> locations = new HashMap<String, Location>();
		public final Map<String, Facility> facilities = new HashMap<String, Facility>();
		public final Map<String, WatchDog> watchdogs = new HashMap<String, WatchDog>();

		public AdapterHolder(Adapter adapter) {
			this.adapter = adapter;
		}
	}

	Map<String, AdapterHolder> mAdapters = new HashMap<String, AdapterHolder>();

	public DemoNetwork(Context context) {
		mContext = context;
	}

	private boolean isAdapterAllowed(String adapterId) {
		return getRandomForAdapter(adapterId).nextBoolean();
	}

	private Random getRandomForAdapter(String adapterId) {
		try {
			int id = Integer.parseInt(adapterId);
			return new Random(id);
		} catch (NumberFormatException e) {
			return new Random();
		}
	}

	private void setNewValue(Device device) {
		Random random = new Random();
		
		if (device.getValue() instanceof BaseEnumValue) {
			BaseEnumValue value = (BaseEnumValue)device.getValue();
			List<Item> items = value.getEnumItems();
			Item item = items.get(random.nextInt(items.size()));
			
			device.setValue(item.getValue());
		} else {
			double lastValue = device.getValue().getDoubleValue();
			double range = 2 + Math.log(device.getFacility().getRefresh().getInterval());

			if (Double.isNaN(lastValue)) {
				lastValue = random.nextDouble() * 1000;
			}
			
			double addvalue = random.nextInt((int) range * 1000) / 1000;
			boolean plus = random.nextBoolean();
			lastValue = lastValue + addvalue * (plus ? 1 : -1);
			
			device.setValue(String.valueOf((int)lastValue));
		}
	}
	
	public void initDemoData() throws AppException {
		// Erase previous data if exists
		mAdapters.clear();
		
		// Set user
		mUser = new User(DEMO_USER_ID, "John", "Doe", "john@doe.com", Gender.Male, Role.Superuser);

		// Set session token
		mBT = DEMO_USER_BT;

		// Parse and set initial demo data
		XmlParsers parser = new XmlParsers();

		String assetName = Constants.ASSET_ADAPTERS_FILENAME;
		for (Adapter adapter : parser.getDemoAdaptersFromAsset(mContext, assetName)) {
			mAdapters.put(adapter.getId(), new AdapterHolder(adapter));
		}

		for (AdapterHolder holder : mAdapters.values()) {
			assetName = String.format(Constants.ASSET_LOCATIONS_FILENAME, holder.adapter.getId());

			for (Location location : parser.getDemoLocationsFromAsset(mContext, assetName)) {
				holder.locations.put(location.getId(), location);
			}

			assetName = String.format(Constants.ASSET_WATCHDOGS_FILENAME, holder.adapter.getId());
			for (WatchDog watchdog : parser.getDemoWatchDogsFromAsset(mContext, assetName)) {
				holder.watchdogs.put(watchdog.getId(), watchdog);
			}

			assetName = String.format(Constants.ASSET_ADAPTER_DATA_FILENAME, holder.adapter.getId());
			for (Facility facility : parser.getDemoFacilitiesFromAsset(mContext, assetName)) {
				holder.facilities.put(facility.getId(), facility);
			}

			// Set last update time to time between (-26 hours, now>
			for (Facility facility : holder.facilities.values()) {
				facility.setLastUpdate(DateTime.now(DateTimeZone.UTC).minusSeconds(new Random().nextInt(60 * 60 * 26)));
			}
		}
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

    @Override
    public String getBT() {
		return mBT;
	}

	@Override
	public void setBT(String token) {
		mBT = token;
	}

	@Override
    public User loadUserInfo() {
		return mUser;
	}

	@Override
	public boolean loginMe(IAuthProvider authProvider) {
		return true;
	}

	@Override
	public boolean registerMe(IAuthProvider authProvider) {
		return true;
	}

	@Override
	public boolean addProvider(IAuthProvider authProvider){return true;}

	@Override
	public boolean removeProvider(String providerName){return true;}

	@Override
	public boolean deleteMyAccount(){return true;}

	@Override
	public boolean addAdapter(String adapterId, String adapterName) {
		if (!isAdapterAllowed(adapterId)) {
			return false;
		}

		Random rand = getRandomForAdapter(adapterId);

		Adapter adapter = new Adapter();
		adapter.setId(adapterId);
		adapter.setName(adapterName);

		// Use random role
		Role[] roles = Role.values();
		adapter.setRole(roles[rand.nextInt(roles.length)]);

		// Use random offset
		adapter.setUtcOffset(rand.nextInt(24 * 60) - 12 * 60);

		mAdapters.put(adapter.getId(), new AdapterHolder(adapter));

		return true;
	}

	@Override
	public List<Adapter> getAdapters() {
		List<Adapter> adapters = new ArrayList<Adapter>();

		for (AdapterHolder holder : mAdapters.values()) {
			adapters.add(holder.adapter);
		}

		return adapters;
	}

	@Override
	public List<Facility> initAdapter(String adapterId) {
		List<Facility> facilities = new ArrayList<Facility>();

		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder != null) {
			Random rand = new Random();

			for (Facility facility : holder.facilities.values()) {
				if (!facility.isInitialized())
					continue;
				
				if (facility.isExpired()) {
					// Set new random values
					facility.setBattery(rand.nextInt(101));
					facility.setLastUpdate(DateTime.now(DateTimeZone.UTC));
					facility.setNetworkQuality(rand.nextInt(101));

					for (Device device : facility.getDevices()) {
						setNewValue(device);
					}
				}

				facilities.add(facility);
			}
		}

		return facilities;
	}

	@Override
	public boolean reInitAdapter(String oldId, String newId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateFacilities(String adapterId, List<Facility> facilities, EnumSet<SaveDevice> toSave) {
		for (Facility facility : facilities) {
			if (!updateFacility(adapterId, facility, toSave)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean updateDevice(String adapterId, Device device, EnumSet<SaveDevice> toSave) {
		// NOTE: this replaces (or add) whole facility, not only device's fields marked as toSave
		return updateFacility(adapterId, device.getFacility(), toSave);
	}

	@Override
	public boolean switchState(String adapterId, Device device) {
		return true;
	}

	@Override
	public boolean prepareAdapterToListenNewSensors(String adapterId) {
		return isAdapterAllowed(adapterId);
	}

	@Override
	public boolean deleteFacility(String adapterId, Facility facility) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return false;
		}

		return holder.facilities.remove(facility.getId()) != null;
	}

	@Override
	public List<Facility> getFacilities(List<Facility> facilities) {
		List<Facility> result = new ArrayList<Facility>();

		for (Facility facility : facilities) {
			Facility newFacility = getFacility(facility);
			if (newFacility != null && newFacility.isInitialized()) {
				result.add(newFacility);
			}
		}

		return result;
	}

	@Override
	public Facility getFacility(Facility facility) {
		AdapterHolder holder = mAdapters.get(facility.getAdapterId());
		if (holder == null) {
			return null;
		}
		
		Random rand = new Random();

		Facility newFacility = holder.facilities.get(facility.getId());

		if (newFacility.isExpired()) {
			// Set new random values
			newFacility.setBattery(rand.nextInt(101));
			newFacility.setLastUpdate(DateTime.now(DateTimeZone.UTC));
			newFacility.setNetworkQuality(rand.nextInt(101));

			for (Device device : newFacility.getDevices()) {
				setNewValue(device);
			}
		}

		return newFacility;
	}

	@Override
	public boolean updateFacility(String adapterId, Facility facility, EnumSet<SaveDevice> toSave) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return false;
		}

		// NOTE: this replaces (or add, in case of initializing new facility) whole facility, not only fields marked as toSave
		holder.facilities.put(facility.getId(), facility);
		return true;
	}

	@Override
	public List<Facility> getNewFacilities(String adapterId) {
		List<Facility> facilities = new ArrayList<Facility>();

		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return facilities;
		}

		Random rand = new Random();
		if (rand.nextInt(4) == 0) {
			Facility facility = new Facility();

			facility.setAdapterId(adapterId);

			// Create unique facility id
			String address;
			int i = 0;
			do {
				address = "10.0.0." + String.valueOf(i++);
			} while (holder.facilities.containsKey(address));

			facility.setAddress(address);
			facility.setBattery(rand.nextInt(101));
			facility.setInitialized(rand.nextBoolean());
			facility.setInvolveTime(DateTime.now(DateTimeZone.UTC));
			facility.setLastUpdate(DateTime.now(DateTimeZone.UTC));
			// facility.setLocationId(locationId); // uninitialized facility has no location
			facility.setNetworkQuality(rand.nextInt(101));

			RefreshInterval[] refresh = RefreshInterval.values();
			facility.setRefresh(refresh[rand.nextInt(refresh.length)]);

			// add device
			int count = rand.nextInt(5);
			do {
				// Create unique device type
				DeviceType[] types = DeviceType.values();
				String typeId = "0";
				do {
					typeId = String.valueOf(rand.nextInt(types.length));
				} while (facility.getDeviceByType(DeviceType.fromValue(typeId)) != null);

				Device device = Device.createFromDeviceTypeId(typeId);
				device.setFacility(facility);
				// device.setName(name); // uninitialized device has no name
				setNewValue(device);
				device.setVisibility(true);

				facility.addDevice(device);
			} while (--count > 0);

			facilities.add(facility);
		}

		return facilities;
	}

	@Override
	public DeviceLog getLog(String adapterId, Device device, LogDataPair pair) {
		// Generate random values for log in demo mode
		DeviceLog log = new DeviceLog(DataType.AVERAGE, DataInterval.RAW);

		double lastValue = pair.device.getValue().getDoubleValue();
		double range = 2 + Math.log(device.getFacility().getRefresh().getInterval());

		long start = pair.interval.getStartMillis();
		long end = pair.interval.getEndMillis();

		Random random = new Random();

		if (Double.isNaN(lastValue)) {
			lastValue = random.nextDouble() * 1000;
		}

		int everyMsecs = Math.max(pair.gap.getValue(), device.getFacility().getRefresh().getInterval()) * 1000;

		boolean isEnum = (device.getValue() instanceof BaseEnumValue);

		while (start < end) {
			if (isEnum) {
				BaseEnumValue value = (BaseEnumValue)device.getValue();
				List<Item> items = value.getEnumItems();
				
				int pos = 0;
				for (Item item : items) {
					if (item.getId() == (int)lastValue) {
						break;
					}
					pos++;
				}
				// (size + pos + <-1,1>) % size  - first size is because it could end up to "-1"
				pos = (items.size() + pos + (random.nextInt(3) - 1)) % items.size();
				lastValue = items.get(pos).getId();
			} else {
				double addvalue = random.nextInt((int) range * 1000) / 1000;
				boolean plus = random.nextBoolean();
				lastValue = lastValue + addvalue * (plus ? 1 : -1);
			}

			log.addValue(start, (float) lastValue);
			start += everyMsecs;
		}

		return log;
	}

	@Override
	public List<Location> getLocations(String adapterId) {
		List<Location> locations = new ArrayList<Location>();

		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder != null) {
			for (Location location : holder.locations.values()) {
				locations.add(location);
			}
		}

		return locations;
	}

	@Override
	public boolean updateLocations(String adapterId, List<Location> locations) {
		for (Location location : locations) {
			if (!updateLocation(adapterId, location)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean updateLocation(String adapterId, Location location) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return false;
		}

		if (!holder.locations.containsKey(location.getId())) {
			return false;
		}

		// NOTE: this replaces (or add) whole facility, not only fields marked as toSave
		holder.locations.put(location.getId(), location);
		return true;
	}

	@Override
	public boolean deleteLocation(String adapterId, Location location) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return false;
		}

		return holder.locations.remove(location.getId()) != null;
	}

	@Override
	public Location createLocation(String adapterId, Location location) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return null;
		}

		// Create unique location id
		String locationId;
		int i = 0;
		do {
			locationId = String.valueOf(i++);
		} while (holder.locations.containsKey(locationId));

		// Set new location id
		location.setId(locationId);

		return location;
	}

	@Override
	public boolean addView(String viewName, int iconID, List<Device> devices) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<CustomViewPair> getViews() {
		// TODO Auto-generated method stub
		return new ArrayList<CustomViewPair>();
	}

	@Override
	public boolean deleteView(String viewName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateView(String viewName, int iconId, Facility facility, NetworkAction action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAccounts(String adapterId, ArrayList<User> users) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAccount(String adapterId, User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteAccounts(String adapterId, List<User> users) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteAccount(String adapterId, User user) {
		// TODO: Actual implementation deletes adapter, not account...
		return mAdapters.remove(adapterId) != null;
	}

	@Override
	public ArrayList<User> getAccounts(String adapterId) {
		// TODO Auto-generated method stub
		return new ArrayList<User>();
	}

	@Override
	public boolean updateAccounts(String adapterId, ArrayList<User> users) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateAccount(String adapterId, User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setTimeZone(String adapterId, int differenceToGMT) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getTimeZone(String adapterId) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return 0;
		}

		return holder.adapter.getUtcOffsetMillis() / (60 * 1000);
	}

	@Override
	public boolean NotificationsRead(ArrayList<String> msgID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Condition setCondition(Condition condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean connectConditionWithAction(String conditionID, String actionID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Condition getCondition(Condition condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Condition> getConditions() {
		// TODO Auto-generated method stub
		return new ArrayList<Condition>();
	}

	@Override
	public boolean updateCondition(Condition condition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCondition(Condition condition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ComplexAction setAction(ComplexAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ComplexAction> getActions() {
		// TODO Auto-generated method stub
		return new ArrayList<ComplexAction>();
	}

	@Override
	public ComplexAction getAction(ComplexAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateAction(ComplexAction action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteAction(ComplexAction action) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public ArrayList<WatchDog> getAllWatchDogs(String adapterID){
		ArrayList<WatchDog> watchdogs = new ArrayList<WatchDog>();

		AdapterHolder holder = mAdapters.get(adapterID);
		if (holder != null) {
			for (WatchDog watchdog : holder.watchdogs.values()) {
				watchdogs.add(watchdog);
			}
		}

		return watchdogs;
	}

    @Override
    public ArrayList<WatchDog> getWatchDogs(ArrayList<String> watchDogIds, String adapterId) {
		return null;
	}

    @Override
    public boolean updateWatchDog(WatchDog watchDog, String AdapterId){
		AdapterHolder holder = mAdapters.get(AdapterId);
		if (holder == null) {
			return false;
		}

		if (!holder.watchdogs.containsKey(watchDog.getId())) {
			return false;
		}

		// NOTE: this replaces (or add) whole watchdog, not only fields marked as toSave
		holder.watchdogs.put(watchDog.getId(), watchDog);
		return true;
	}

    @Override
    public boolean deleteWatchDog(WatchDog watchDog){
		AdapterHolder holder = mAdapters.get(watchDog.getAdapterId());
		if (holder == null) {
			return false;
		}

		return holder.watchdogs.remove(watchDog.getId()) != null;
	}

    @Override
    public boolean addWatchDog(WatchDog watchDog, String AdapterID){
		AdapterHolder holder = mAdapters.get(AdapterID);
		if (holder == null) {
			return false;
		}

		// Create unique watchdog id
		String watchdogId;
		int i = 0;
		do {
			watchdogId = String.valueOf(i++);
		} while (holder.watchdogs.containsKey(watchdogId));

		// Set new location id
		watchDog.setId(watchdogId);
		holder.watchdogs.put(watchdogId, watchDog);
		return true;
	}

    @Override
    public boolean passBorder(String regionId, String type) {
		return true;
	}
}
