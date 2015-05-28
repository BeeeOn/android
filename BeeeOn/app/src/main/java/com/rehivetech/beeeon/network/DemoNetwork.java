package com.rehivetech.beeeon.network;

import android.content.Context;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.ModuleLog.DataInterval;
import com.rehivetech.beeeon.household.device.ModuleLog.DataType;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue.Item;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Gender;
import com.rehivetech.beeeon.household.user.User.Role;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.XmlParsers;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.util.DataHolder;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
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

	private static final int RAW_ENUM_VALUES_COUNT_IN_LOG = 100;

	private Context mContext;
	private User mUser;
	private String mBT;
	private boolean mInitialized;
	private Map<String, Random> mRandoms = new HashMap<>();

	public final DataHolder<Adapter> mAdapters = new DataHolder<>();
	public final MultipleDataHolder<Location> mLocations = new MultipleDataHolder<>();
	public final MultipleDataHolder<Device> mDevices = new MultipleDataHolder<>();
	public final MultipleDataHolder<Watchdog> mWatchdogs = new MultipleDataHolder<>();
	public final MultipleDataHolder<AchievementListItem> mAchievements = new MultipleDataHolder<>();
	public final MultipleDataHolder<User> mUsers = new MultipleDataHolder<>();

	public DemoNetwork(Context context) {
		mContext = context;

		String demoModeString = mContext.getString(R.string.demo_mode);

		// Set user
		mUser = new User(DEMO_USER_ID, demoModeString, "", demoModeString, Gender.UNKNOWN, Role.Superuser);

		// Set session token
		mBT = DEMO_USER_BT;
	}

	private boolean isAdapterAllowed(String adapterId) {
		return getRandomForAdapter(adapterId).nextBoolean();
	}

	private Random getRandomForAdapter(String adapterId) {
		Random rand = mRandoms.get(adapterId);

		if (rand == null) {
			try {
				int id = Integer.parseInt(adapterId);
				rand = new Random(id);
			} catch (NumberFormatException e) {
				rand = new Random();
			}
			mRandoms.put(adapterId, rand);
		}

		return rand;
	}

	private void setNewValue(Module module) {
		// Don't set new values for actors (unless it's the first value to be set during initialization)
		if (module.getType().isActor() && module.getValue().hasValue())
			return;

		Random rand = getRandomForAdapter(module.getDevice().getAdapterId());
		
		if (module.getValue() instanceof BaseEnumValue) {
			BaseEnumValue value = (BaseEnumValue) module.getValue();
			List<Item> items = value.getEnumItems();
			Item item = items.get(rand.nextInt(items.size()));
			
			module.setValue(item.getValue());
		} else {
			double lastValue = module.getValue().getDoubleValue();
			double range = 2 + Math.log(module.getDevice().getRefresh().getInterval());

			if (Double.isNaN(lastValue)) {
				lastValue = rand.nextDouble() * 1000;
			}
			
			double addvalue = rand.nextInt((int) range * 1000) / 1000;
			boolean plus = rand.nextBoolean();
			lastValue = lastValue + addvalue * (plus ? 1 : -1);
			
			module.setValue(String.valueOf((int)lastValue));
		}
	}

	public void initDemoData() throws AppException {
		if (mInitialized)
			return;

		// Erase previous data if exists
		mAdapters.clear();
		mLocations.clear();
		mDevices.clear();
		mWatchdogs.clear();
		mAchievements.clear();
		mUsers.clear();

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
			mWatchdogs.setObjects(adapterId, parser.getDemoWatchdogsFromAsset(mContext, assetName));
			mWatchdogs.setLastUpdate(adapterId, DateTime.now());

			assetName = String.format(Constants.ASSET_ADAPTER_DATA_FILENAME, adapter.getId());
			mDevices.setObjects(adapterId, parser.getDemoDevicesFromAsset(mContext, assetName));
			mDevices.setLastUpdate(adapterId, DateTime.now());

			assetName = String.format(Constants.ASSET_ACHIEVEMENTS_FILENAME, adapter.getId());
			mAchievements.setObjects(adapterId, parser.getDemoAchievementsFromAsset(mContext, assetName));
			mAchievements.setLastUpdate(adapterId, DateTime.now());

			// Just one (self) user for now, anyone can create XML with more users and use it here like other items
			mUsers.setObjects(adapterId, Arrays.asList(new User[]{ new User(mUser.getId(), "John", "Doe", "john@doe.com", Gender.MALE, Role.Superuser) }));

			Random rand = getRandomForAdapter(adapter.getId());

			// Set last update time to time between (-26 hours, now>
			for (Device device : mDevices.getObjects(adapterId)) {
				// FIXME: is using getObjects() ok? It creates new list. But it should be ok, because inner objects are still only references. Needs test!
				device.setLastUpdate(DateTime.now(DateTimeZone.UTC).minusSeconds(rand.nextInt(60 * 60 * 26)));
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
	public List<Device> initAdapter(String adapterId) {
		List<Device> devices = mDevices.getObjects(adapterId);

		Random rand = getRandomForAdapter(adapterId);

		// Update value of expired devices
		for (Device device : devices) {
			if (device.isExpired()) {
				// Set new random values
				device.setBattery(rand.nextInt(101));
				device.setLastUpdate(DateTime.now(DateTimeZone.UTC));
				device.setNetworkQuality(rand.nextInt(101));

				for (Module module : device.getModules()) {
					setNewValue(module);
				}
			}
		}

		// TODO: I think this is not necessary
		// Remove uninitialized devices from this list
		for (Iterator<Device> it = devices.iterator(); it.hasNext(); ) {
			if (!it.next().isInitialized()) {
				it.remove();
			}
		}

		return devices;
	}

	@Override
	public boolean reInitAdapter(String oldId, String newId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateDevices(String adapterId, List<Device> devices, EnumSet<Module.SaveModule> toSave) {
		for (Device device : devices) {
			if (!updateFacility(adapterId, device, toSave)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean updateModule(String adapterId, Module module, EnumSet<SaveModule> toSave) {
		// NOTE: this replaces (or add) whole mDevice, not only module's fields marked as toSave
		return updateFacility(adapterId, module.getDevice(), toSave);
	}

	@Override
	public boolean switchState(String adapterId, Module module) {
		return true;
	}

	@Override
	public boolean prepareAdapterToListenNewSensors(String adapterId) {
		return isAdapterAllowed(adapterId);
	}

	@Override
	public boolean deleteFacility(Device device) {
		return mDevices.removeObject(device.getAdapterId(), device.getId()) != null;
	}

	@Override
	public List<Device> getDevices(List<Device> devices) {
		List<Device> result = new ArrayList<Device>();

		for (Device device : devices) {
			Device newDevice = getFacility(device);
			if (newDevice != null && newDevice.isInitialized()) {
				result.add(newDevice);
			}
		}

		return result;
	}

	@Override
	public Device getFacility(Device device) {
		Random rand = getRandomForAdapter(device.getAdapterId());

		Device newDevice = mDevices.getObject(device.getAdapterId(), device.getId());

		if (newDevice != null && newDevice.isExpired()) {
			// Set new random values
			newDevice.setBattery(rand.nextInt(101));
			newDevice.setLastUpdate(DateTime.now(DateTimeZone.UTC));
			newDevice.setNetworkQuality(rand.nextInt(101));

			for (Module module : newDevice.getModules()) {
				setNewValue(module);
			}
		}

		return newDevice;
	}

	@Override
	public boolean updateFacility(String adapterId, Device device, EnumSet<Module.SaveModule> toSave) {
		// NOTE: this replaces (or add, in case of initializing new mDevice) whole mDevice, not only fields marked as toSave
		mDevices.addObject(adapterId, device);
		return true;
	}

	@Override
	public List<Device> getNewDevices(String adapterId) {
		List<Device> newDevices = new ArrayList<>();

		if (!mAdapters.hasObject(adapterId)) {
			return newDevices;
		}

		Random rand = getRandomForAdapter(adapterId);
		if (rand.nextInt(4) == 0) {
			Device device = new Device();

			device.setAdapterId(adapterId);

			// Create unique mDevice id
			String address;
			int i = 0;
			do {
				address = "10.0.0." + String.valueOf(i++);
			} while (mDevices.hasObject(adapterId, address));

			device.setAddress(address);
			device.setBattery(rand.nextInt(101));
			device.setInitialized(rand.nextBoolean());
			device.setInvolveTime(DateTime.now(DateTimeZone.UTC));
			device.setLastUpdate(DateTime.now(DateTimeZone.UTC));
			// mDevice.setLocationId(locationId); // uninitialized mDevice has no location
			device.setNetworkQuality(rand.nextInt(101));

			RefreshInterval[] refresh = RefreshInterval.values();
			device.setRefresh(refresh[rand.nextInt(refresh.length)]);

			// add random number of devices (max. 5)
			int count = rand.nextInt(5);
			do {
				// Get random module type
				ModuleType[] types = ModuleType.values();
				ModuleType randType = types[rand.nextInt(types.length)];

				// Determine offset (number of existing devices with this type in the mDevice)
				int offset = 0;
				for (Module module : device.getModules()) {
					if (module.getType() == randType) {
						offset++;
					}
				}

				// Create combined module type
				String typeId = String.valueOf(offset * 256 + randType.getTypeId());

				// Create default name
				String defaultName = String.format("%s %d", mContext.getString(randType.getStringResource()), offset + 1);

				Module module = Module.createFromModuleTypeId(typeId);
				module.setDevice(device);
				module.setName(defaultName);
				module.setVisibility(true);
				setNewValue(module);

				device.addModule(module);
			} while (--count >= 0);

			// Add new mDevice to global holder
			mDevices.addObject(adapterId, device);

			// Add to list that we return
			newDevices.add(device);
		}

		return newDevices;
	}

	@Override
	public ModuleLog getLog(String adapterId, Module module, LogDataPair pair) {
		// Generate random values for log in demo mode
		ModuleLog log = new ModuleLog(DataType.AVERAGE, DataInterval.RAW);

		double lastValue = pair.module.getValue().getDoubleValue();
		double range = 2 + Math.log(module.getDevice().getRefresh().getInterval());

		long start = pair.interval.getStartMillis();
		long end = pair.interval.getEndMillis();

		Random rand = getRandomForAdapter(adapterId);

		if (Double.isNaN(lastValue)) {
			lastValue = rand.nextDouble() * 1000;
		}

		int everyMsecs = Math.max(pair.gap.getSeconds(), module.getDevice().getRefresh().getInterval()) * 1000;

		boolean isEnum = (module.getValue() instanceof BaseEnumValue);

		if (isEnum) {
			// For enums we want fixed number of steps (because application surely wants raw values)
			everyMsecs = (int)(end - start) / RAW_ENUM_VALUES_COUNT_IN_LOG;
		}

		while (start < end) {
			if (isEnum) {
				BaseEnumValue value = (BaseEnumValue) module.getValue();
				List<Item> items = value.getEnumItems();
				
				int pos = 0;
				for (Item item : items) {
					if (item.getId() == (int)lastValue) {
						break;
					}
					pos++;
				}
				// (size + pos + <-1,1>) % size  - first size is because it could end up to "-1"
				pos = (items.size() + pos + (rand.nextInt(3) - 1)) % items.size();
				lastValue = items.get(pos).getId();
			} else {
				double addvalue = rand.nextInt((int) range * 1000) / 1000;
				boolean plus = rand.nextBoolean();
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
	public boolean addAccounts(String adapterId, ArrayList<User> users) {
		for (User user : users) {
			if (!addAccount(adapterId, user)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean addAccount(String adapterId, User user) {
		if (!mAdapters.hasObject(adapterId)) {
			return false;
		}

		// Create unique user id
		String userId;
		int i = 0;
		do {
			userId = String.valueOf(i++);
		} while (mUsers.hasObject(adapterId, userId));

		// Set new user id
		user.setId(userId);

		// Set user name to his e-mail, because names must provide server (and we don't have any users here)
		user.setName(user.getEmail());

		mUsers.addObject(adapterId, user);
		return true;
	}

	@Override
	public boolean deleteAccounts(String adapterId, List<User> users) {
		for (User user : users) {
			if (!deleteAccount(adapterId, user)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean deleteAccount(String adapterId, User user) {
		// TODO: Actual implementation deletes adapter, not account...
		if (user.getId().equals(mUser.getId())) {
			// If we're deleting ourselves, remove whole adapter
			mLocations.removeHolder(adapterId);
			mDevices.removeHolder(adapterId);
			mWatchdogs.removeHolder(adapterId);
			mAchievements.removeHolder(adapterId);
			return mAdapters.removeObject(adapterId) != null;
		} else {
			// TODO: This is correct implementation for future
			return mUsers.removeObject(adapterId, user.getId()) != null;
		}
	}

	@Override
	public List<User> getAccounts(String adapterId) {
		return mUsers.getObjects(adapterId);
	}

	@Override
	public boolean updateAccounts(String adapterId, ArrayList<User> users) {
		for (User user : users) {
			if (!updateAccount(adapterId, user)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean updateAccount(String adapterId, User user) {
		if (!mAdapters.hasObject(adapterId)) {
			return false;
		}

		// NOTE: this replaces (or add) whole user
		mUsers.addObject(adapterId, user);

		// We can have only one superuser, so unset all other superusers of this adapter (this does classic server too)
		if (user.getRole() == Role.Superuser) {
			for (User otherUser : mUsers.getObjects(adapterId)) {
				// Change only other users, except me
				if (!otherUser.getId().equals(user.getId())) {
					continue;
				}

				// If their role is superuser, switch them to just admin
				if (otherUser.getRole() == Role.Superuser) {
					otherUser.setRole(Role.Admin);
				}
			}
		}

		// If we change role of ourselves, we need to put such change to adapter itself too
		if (user.getId().equals(mUser.getId())) {
			Adapter adapter = mAdapters.getObject(adapterId);
			adapter.setRole(user.getRole());
		}

		return true;
	}

	@Override
	public boolean setTimeZone(String adapterId, int offsetInMinutes) {
		Adapter adapter = mAdapters.getObject(adapterId);

		if (adapter == null) {
			return false;
		}

		adapter.setUtcOffset(offsetInMinutes);
		return true;
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
	public List<VisibleNotification> getNotifications() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
    public List<Watchdog> getAllWatchdogs(String adapterId) {
		return mWatchdogs.getObjects(adapterId);
	}

    @Override
    public List<Watchdog> getWatchdogs(ArrayList<String> watchdogIds, String adapterId) {
		return new ArrayList<>();
	}

    @Override
    public boolean updateWatchdog(Watchdog watchdog, String adapterId) {
		if (!mAdapters.hasObject(adapterId)) {
			return false;
		}

		// NOTE: this replaces (or add) whole watchdog
		mWatchdogs.addObject(adapterId, watchdog);
		return true;
	}

    @Override
    public boolean deleteWatchdog(Watchdog watchdog){
		return mWatchdogs.removeObject(watchdog.getAdapterId(), watchdog.getId()) != null;
	}

    @Override
    public boolean addWatchdog(Watchdog watchdog, String adapterId){
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
		watchdog.setId(watchdogId);
		mWatchdogs.addObject(adapterId, watchdog);
		return true;
	}

    @Override
    public boolean passBorder(String regionId, String type) {
		return true;
	}

	@Override
	public List<AchievementListItem> getAllAchievements(String adapterID){ return mAchievements.getObjects(adapterID); }

	@Override
	public List<String> setProgressLvl(String adapterId, String achievementId){ return new ArrayList<>(); }
}
