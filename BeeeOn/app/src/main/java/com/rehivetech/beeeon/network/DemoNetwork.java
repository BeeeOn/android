package com.rehivetech.beeeon.network;

import android.content.Context;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Device.SaveDevice;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.DeviceLog.DataInterval;
import com.rehivetech.beeeon.household.device.DeviceLog.DataType;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue.Item;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Gender;
import com.rehivetech.beeeon.household.user.User.Role;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.CustomViewPair;
import com.rehivetech.beeeon.network.xml.XmlParsers;
import com.rehivetech.beeeon.network.xml.action.ComplexAction;
import com.rehivetech.beeeon.network.xml.condition.Condition;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.util.DataHolder;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
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
	private boolean mInitialized;
	private Map<String, Random> mRandoms = new HashMap<>();

	public final DataHolder<Adapter> mAdapters = new DataHolder<>();
	public final MultipleDataHolder<Location> mLocations = new MultipleDataHolder<>();
	public final MultipleDataHolder<Facility> mFacilities = new MultipleDataHolder<>();
	public final MultipleDataHolder<WatchDog> mWatchdogs = new MultipleDataHolder<>();

	public DemoNetwork(Context context) {
		mContext = context;

		// Set user
		mUser = new User(DEMO_USER_ID, "John", "Doe", "john@doe.com", Gender.Male, Role.Superuser);

		// Set session token
		mBT = DEMO_USER_BT;
	}

	private boolean isAdapterAllowed(String adapterId) {
		return getRandomForAdapter(adapterId).nextBoolean();
	}

	private Random getRandomForAdapter(String adapterId) {
		Random random = mRandoms.get(adapterId);

		if (random == null) {
			try {
				int id = Integer.parseInt(adapterId);
				random = new Random(id);
			} catch (NumberFormatException e) {
				random = new Random();
			}
			mRandoms.put(adapterId, random);
		}

		return random;
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
		if (mInitialized)
			return;

		// Erase previous data if exists
		mAdapters.clear();
		mLocations.clear();
		mFacilities.clear();
		mWatchdogs.clear();

		// Parse and set initial demo data
		XmlParsers parser = new XmlParsers();

		String assetName = Constants.ASSET_ADAPTERS_FILENAME;
		mAdapters.setObjects(parser.getDemoAdaptersFromAsset(mContext, assetName));

		for (Adapter adapter : mAdapters.getObjects()) {
			String adapterId = adapter.getId();

			assetName = String.format(Constants.ASSET_LOCATIONS_FILENAME, adapter.getId());
			mLocations.setObjects(adapterId, parser.getDemoLocationsFromAsset(mContext, assetName));
			mLocations.setLastUpdate(adapterId, DateTime.now());

			assetName = String.format(Constants.ASSET_WATCHDOGS_FILENAME, adapter.getId());
			mWatchdogs.setObjects(adapterId, parser.getDemoWatchDogsFromAsset(mContext, assetName));
			mWatchdogs.setLastUpdate(adapterId, DateTime.now());

			assetName = String.format(Constants.ASSET_ADAPTER_DATA_FILENAME, adapter.getId());
			mFacilities.setObjects(adapterId, parser.getDemoFacilitiesFromAsset(mContext, assetName));
			mFacilities.setLastUpdate(adapterId, DateTime.now());

			// Set last update time to time between (-26 hours, now>
			for (Facility facility : mFacilities.getObjects(adapterId)) {
				// FIXME: is using getObjects() ok? It creates new list. But it should be ok, because inner objects are still only references. Needs test!
				facility.setLastUpdate(DateTime.now(DateTimeZone.UTC).minusSeconds(new Random().nextInt(60 * 60 * 26)));
			}
		}

		mInitialized = true;
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
	public boolean hasBT() {
		return !mBT.isEmpty();
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
	public boolean addProvider(IAuthProvider authProvider) {
		return true;
	}

	@Override
	public boolean removeProvider(String providerName) {
		return true;
	}

	@Override
	public boolean deleteMyAccount() {
		return true;
	}

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

		mAdapters.addObject(adapter);

		return true;
	}

	@Override
	public List<Adapter> getAdapters() {
		// Init demo data, if not initialized yet
		initDemoData();

		return mAdapters.getObjects();
	}

	@Override
	public List<Facility> initAdapter(String adapterId) {
		List<Facility> facilities = mFacilities.getObjects(adapterId);

		Random rand = new Random();

		// Update value of expired facilities
		for (Facility facility : facilities) {
			if (facility.isExpired()) {
				// Set new random values
				facility.setBattery(rand.nextInt(101));
				facility.setLastUpdate(DateTime.now(DateTimeZone.UTC));
				facility.setNetworkQuality(rand.nextInt(101));

				for (Device device : facility.getDevices()) {
					setNewValue(device);
				}
			}
		}

		// TODO: I think this is not necessary
		// Remove uninitialized facilities from this list
		for (Iterator<Facility> it = facilities.iterator(); it.hasNext(); ) {
			if (!it.next().isInitialized()) {
				it.remove();
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
	public boolean deleteFacility(Facility facility) {
		return mFacilities.removeObject(facility.getAdapterId(), facility.getId()) != null;
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
		Random rand = new Random();

		Facility newFacility = mFacilities.getObject(facility.getAdapterId(), facility.getId());

		if (newFacility != null && newFacility.isExpired()) {
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
		// NOTE: this replaces (or add, in case of initializing new facility) whole facility, not only fields marked as toSave
		mFacilities.addObject(adapterId, facility);
		return true;
	}

	@Override
	public List<Facility> getNewFacilities(String adapterId) {
		List<Facility> newFacilities = new ArrayList<>();

		if (!mAdapters.hasObject(adapterId)) {
			return newFacilities;
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
			} while (mFacilities.hasObject(adapterId, address));

			facility.setAddress(address);
			facility.setBattery(rand.nextInt(101));
			facility.setInitialized(rand.nextBoolean());
			facility.setInvolveTime(DateTime.now(DateTimeZone.UTC));
			facility.setLastUpdate(DateTime.now(DateTimeZone.UTC));
			// facility.setLocationId(locationId); // uninitialized facility has no location
			facility.setNetworkQuality(rand.nextInt(101));

			RefreshInterval[] refresh = RefreshInterval.values();
			facility.setRefresh(refresh[rand.nextInt(refresh.length)]);

			// add random number of devices (max. 5)
			int count = rand.nextInt(5);
			do {
				// Get random device type
				DeviceType[] types = DeviceType.values();
				DeviceType randType = types[rand.nextInt(types.length)];

				// Determine offset (number of existing devices with this type in the facility)
				int offset = 0;
				for (Device device : facility.getDevices()) {
					if (device.getType() == randType) {
						offset++;
					}
				}

				// Create combined device type
				String typeId = String.valueOf(offset * 256 + randType.getTypeId());

				// Create default name
				String defaultName = String.format("%s %d", mContext.getString(randType.getStringResource()), offset + 1);

				Device device = Device.createFromDeviceTypeId(typeId);
				device.setFacility(facility);
				device.setName(defaultName);
				device.setVisibility(true);
				setNewValue(device);

				facility.addDevice(device);
			} while (--count >= 0);

			// Add new facility to global holder
			mFacilities.addObject(adapterId, facility);

			// Add to list that we return
			newFacilities.add(facility);
		}

		return newFacilities;
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
		return mLocations.getObjects(adapterId);
	}

	@Override
	public boolean updateLocations(String adapterId, List<Location> locations) {
		for (Location location : locations) {
			if (!updateLocation(location)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean updateLocation(Location location) {
		String adapterId = location.getAdapterId();

		if (!mAdapters.hasObject(adapterId)) {
			return false;
		}

		// NOTE: this replaces (or add) whole location
		mLocations.addObject(adapterId, location);
		return true;
	}

	@Override
	public boolean deleteLocation(Location location) {
		return mLocations.removeObject(location.getAdapterId(), location.getId()) != null;
	}

	@Override
	public Location createLocation(Location location) {
		String adapterId = location.getAdapterId();

		if (!mAdapters.hasObject(adapterId)) {
			return null;
		}

		// Create unique location id
		String locationId;
		int i = 0;
		do {
			locationId = String.valueOf(i++);
		} while (mLocations.hasObject(adapterId, locationId));

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
		mLocations.removeHolder(adapterId);
		mFacilities.removeHolder(adapterId);
		mWatchdogs.removeHolder(adapterId);
		return mAdapters.removeObject(adapterId) != null;
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
		Adapter adapter = mAdapters.getObject(adapterId);

		if (adapter == null) {
			return 0;
		}

		return adapter.getUtcOffsetMillis() / (60 * 1000);
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
    public List<WatchDog> getAllWatchDogs(String adapterId) {
		return mWatchdogs.getObjects(adapterId);
	}

    @Override
    public List<WatchDog> getWatchDogs(ArrayList<String> watchDogIds, String adapterId) {
		return null;
	}

    @Override
    public boolean updateWatchDog(WatchDog watchDog, String adapterId) {
		if (!mAdapters.hasObject(adapterId)) {
			return false;
		}

		// NOTE: this replaces (or add) whole watchdog
		mWatchdogs.addObject(adapterId, watchDog);
		return true;
	}

    @Override
    public boolean deleteWatchDog(WatchDog watchDog){
		return mWatchdogs.removeObject(watchDog.getAdapterId(), watchDog.getId()) != null;
	}

    @Override
    public boolean addWatchDog(WatchDog watchDog, String adapterId){
		if (!mAdapters.hasObject(adapterId)) {
			return false;
		}

		// Create unique watchdog id
		String watchdogId;
		int i = 0;
		do {
			watchdogId = String.valueOf(i++);
		} while (mWatchdogs.hasObject(adapterId, watchdogId));

		// Set new watchdog id
		watchDog.setId(watchdogId);
		mWatchdogs.addObject(adapterId, watchDog);
		return true;
	}

    @Override
    public boolean passBorder(String regionId, String type) {
		return true;
	}

	@Override
	public ArrayList<AchievementListItem> getAllAchievements(String adapterID){ return null; }

	@Override
	public boolean setProgressLvl(String adapterId, String achievementId){ return true; }
}
