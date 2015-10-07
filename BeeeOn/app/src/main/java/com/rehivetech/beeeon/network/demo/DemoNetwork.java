package com.rehivetech.beeeon.network.demo;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.ModuleLog.DataInterval;
import com.rehivetech.beeeon.household.device.ModuleLog.DataType;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.device.values.EnumValue.Item;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Gender;
import com.rehivetech.beeeon.household.user.User.Role;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.util.DataHolder;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
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

	private static final int RAW_ENUM_VALUES_COUNT_IN_LOG = 100;

	private Context mContext;
	private User mUser;
	private String mBT;
	private boolean mInitialized;
	private Map<String, Random> mRandoms = new HashMap<>();

	public final DataHolder<Gate> mGates = new DataHolder<>();
	public final MultipleDataHolder<Location> mLocations = new MultipleDataHolder<>();
	public final MultipleDataHolder<Device> mDevices = new MultipleDataHolder<>();
	public final MultipleDataHolder<User> mUsers = new MultipleDataHolder<>();

	public DemoNetwork(Context context) {
		mContext = context;

		String demoModeName = mContext.getString(R.string.demo_network_demo_mode);
		String demoModeEmail = mContext.getString(R.string.demo_network_demo_mode_email);

		// Set user
		mUser = new User(DEMO_USER_ID, demoModeName, "", demoModeEmail, Gender.UNKNOWN, Role.Superuser);

		// Set session token
		mBT = DEMO_USER_BT;
	}

	private boolean isGateAllowed(String gateId) {
		return getRandomForGate(gateId).nextBoolean();
	}

	private Random getRandomForGate(String gateId) {
		Random rand = mRandoms.get(gateId);

		if (rand == null) {
			try {
				int id = Integer.parseInt(gateId);
				rand = new Random(id);
			} catch (NumberFormatException e) {
				rand = new Random();
			}
			mRandoms.put(gateId, rand);
		}

		return rand;
	}

	private void setNewValue(Module module) {
		// Don't set new values for actors (unless it's the first value to be set during initialization)
		if (module.isActuator() && module.getValue().hasValue())
			return;

		Random rand = getRandomForGate(module.getDevice().getGateId());

		if (module.getValue() instanceof EnumValue) {
			EnumValue value = (EnumValue) module.getValue();
			List<Item> items = value.getEnumItems();
			Item item = items.get(rand.nextInt(items.size()));

			module.setValue(item.getValue());
		} else {
			double lastValue = module.getValue().getDoubleValue();
			double range = 5;

			RefreshInterval refresh = module.getDevice().getRefresh();
			if (refresh != null) {
				range = 2 + Math.log(refresh.getInterval());
			}

			if (Double.isNaN(lastValue)) {
				lastValue = rand.nextDouble() * 1000;
			}

			double addvalue = rand.nextInt((int) range * 1000) / 1000;
			boolean plus = rand.nextBoolean();
			lastValue = lastValue + addvalue * (plus ? 1 : -1);

			module.setValue(String.valueOf((int) lastValue));
		}
	}

	public void initDemoData() throws AppException {
		if (mInitialized)
			return;

		// Erase previous data if exists
		mGates.clear();
		mLocations.clear();
		mDevices.clear();
		mUsers.clear();

		DemoData demoData = new DemoData();
		mGates.setObjects(demoData.getGates(mContext));
		for (Gate gate : mGates.getObjects()) {
			String gateId = gate.getId();

			mLocations.setObjects(gateId, demoData.getLocation(mContext, gateId));
			mLocations.setLastUpdate(gateId, DateTime.now());

			mDevices.setObjects(gateId, demoData.getDevices(gateId));
			mDevices.setLastUpdate(gateId, DateTime.now());

			// Just one (self) user for now, anyone can create XML with more users and use it here like other items
			mUsers.setObjects(gateId, Arrays.asList(new User(mUser.getId(), "John", "Doe", "john@doe.com", Gender.MALE, Role.Superuser)));
			mUsers.setLastUpdate(gateId, DateTime.now());

			Random rand = getRandomForGate(gate.getId());

			// Set last update time to time between (-26 hours, now>
			for (Device device : mDevices.getObjects(gateId)) {
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
	public String getSessionId() {
		return mBT;
	}

	@Override
	public void setSessionId(String token) {
		mBT = token;
	}

	@Override
	public boolean hasSessionId() {
		return !mBT.isEmpty();
	}

	@Override
	public User accounts_getMyProfile() {
		return mUser;
	}

	@Override
	public boolean accounts_logout() {
		return true;
	}

	@Override
	public boolean accounts_login(IAuthProvider authProvider) {
		return true;
	}

	@Override
	public boolean accounts_register(IAuthProvider authProvider) {
		return true;
	}

	@Override
	public boolean accounts_connectAuthProvider(IAuthProvider authProvider) {
		return true;
	}

	@Override
	public boolean accounts_disconnectAuthProvider(String providerName) {
		return true;
	}

	@Override
	public boolean gates_register(String gateId, String gateName, int offsetInMinutes) {
		if (!isGateAllowed(gateId)) {
			return false;
		}

		Random rand = getRandomForGate(gateId);

		Gate gate = new Gate(gateId, gateName);
		gate.setUtcOffset(offsetInMinutes);

		// Use random role
		Role[] roles = Role.values();
		gate.setRole(roles[rand.nextInt(roles.length)]);

		mGates.addObject(gate);

		return true;
	}

	@Override
	public List<Gate> gates_getAll() {
		// Init demo data, if not initialized yet
		initDemoData();

		return mGates.getObjects();
	}

	@Override
	public GateInfo gates_get(String gateId) {
		Gate gate = mGates.getObject(gateId);
		if (gate == null)
			return null;

		int devicesCount = mDevices.getObjects(gateId).size();
		int usersCount = mUsers.getObjects(gateId).size();
		String version = "0";
		String ip = "0.0.0.0";

		return new GateInfo(
				gate.getId(),
				gate.getName(),
				gate.getRole(),
				gate.getUtcOffset(),
				devicesCount,
				usersCount,
				version,
				ip);
	}

	@Override
	public List<Device> devices_getAll(String gateId) {
		List<Device> devices = mDevices.getObjects(gateId);

		Random rand = getRandomForGate(gateId);

		// Update value of expired devices
		for (Device device : devices) {
			if (device.isExpired()) {
				// Set new random values
				Integer battery = device.getBattery();
				if (battery != null) {
					device.setBattery(rand.nextInt(101));
				}
				device.setLastUpdate(DateTime.now(DateTimeZone.UTC));
				device.setNetworkQuality(rand.nextInt(101));

				for (Module module : device.getAllModules(true)) {
					setNewValue(module);
				}
			}
		}

		return devices;
	}

	@Override
	public boolean gates_update(Gate gate) {
		String gateId = gate.getId();

		Gate oldGate = mGates.getObject(gateId);
		if (oldGate == null)
			return false;

		oldGate.setName(gate.getName());
		oldGate.setUtcOffset(gate.getUtcOffset());
		return true;
	}

	@Override
	public boolean gates_unregister(String gateId) {
		return mGates.removeObject(gateId) != null;
	}

	@Override
	public boolean devices_update(String gateId, List<Device> devices) {
		for (Device device : devices) {
			if (!devices_update(gateId, device)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean devices_setState(String gateId, Module module) {
		return true;
	}

	@Override
	public boolean gates_startListen(String gateId) {
		return isGateAllowed(gateId);
	}

	@Override
	public boolean devices_unregister(Device device) {
		return mDevices.removeObject(device.getGateId(), device.getId()) != null;
	}

	@Override
	public List<Device> devices_get(List<Device> devices) {
		List<Device> result = new ArrayList<>();

		for (Device device : devices) {
			Device newDevice = devices_get(device);
			if (newDevice != null) {
				result.add(newDevice);
			}
		}

		return result;
	}

	@Override
	public Device devices_get(Device device) {
		Random rand = getRandomForGate(device.getGateId());

		Device newDevice = mDevices.getObject(device.getGateId(), device.getId());

		if (newDevice != null && newDevice.isExpired()) {
			// Set new random values
			Integer battery = newDevice.getBattery();
			if (battery != null) {
				newDevice.setBattery(rand.nextInt(101));
			}
			newDevice.setLastUpdate(DateTime.now(DateTimeZone.UTC));
			newDevice.setNetworkQuality(rand.nextInt(101));

			for (Module module : newDevice.getAllModules(true)) {
				setNewValue(module);
			}
		}

		return newDevice;
	}

	@Override
	public boolean devices_update(String gateId, Device device) {
		// NOTE: this replaces (or add, in case of initializing new device) whole device
		mDevices.addObject(gateId, device);
		return true;
	}

	@Override
	public List<Device> devices_getNew(String gateId) {
		List<Device> newDevices = new ArrayList<>();

		if (!mGates.hasObject(gateId)) {
			return newDevices;
		}

		Random rand = getRandomForGate(gateId);
		if (rand.nextInt(10) == 0) {
			// Create unique mDevice id
			String address;
			int i = 0;
			do {
				address = "10.0.0." + String.valueOf(i++);
			} while (mDevices.hasObject(gateId, address));

			// Get random device type
			DeviceType[] types = DeviceType.values();
			DeviceType randType = types[1 + rand.nextInt(types.length - 1)]; // 1+ because we don't want unknown type, which is on beginning

			// Create new device
			Device device = Device.createDeviceByType(randType.getId(), gateId, address);

			Integer battery = device.getBattery();
			if (battery != null) {
				device.setBattery(rand.nextInt(101));
			}
			RefreshInterval refresh = device.getRefresh();
			if (refresh != null) {
				RefreshInterval[] refreshes = RefreshInterval.values();
				device.setRefresh(refreshes[rand.nextInt(refreshes.length)]);
			}

			device.setPairedTime(DateTime.now(DateTimeZone.UTC));
			device.setLastUpdate(DateTime.now(DateTimeZone.UTC));
			// mDevice.setLocationId(locationId); // uninitialized mDevice has no location
			device.setNetworkQuality(rand.nextInt(101));

			// Set random values for device modules
			for (Module module : device.getAllModules(true)) {
				setNewValue(module);
			}

			// Add to list that we return
			newDevices.add(device);
		}

		return newDevices;
	}

	@Override
	public ModuleLog devices_getLog(String gateId, Module module, ModuleLog.DataPair pair) {
		// Generate random values for log in demo mode
		ModuleLog log = new ModuleLog(DataType.AVERAGE, DataInterval.RAW);

		long start = pair.interval.getStartMillis();
		long end = pair.interval.getEndMillis();

		Random rand = getRandomForGate(gateId);

		double lastValue = pair.module.getValue().getDoubleValue();
		if (Double.isNaN(lastValue)) {
			lastValue = rand.nextDouble() * 1000;
		}

		boolean isEnum = (module.getValue() instanceof EnumValue);
		boolean hasRefresh = (module.getDevice().getRefresh() != null);

		int everyMsecs;
		double range = 5;

		if (isEnum || !hasRefresh) {
			// For enums we want fixed number of steps (because application surely wants raw values)
			// For devices without refresh it is the similar situation
			everyMsecs = (int) (end - start) / RAW_ENUM_VALUES_COUNT_IN_LOG;
		} else {
			int refreshInterval = module.getDevice().getRefresh().getInterval();
			everyMsecs = Math.max(pair.gap.getSeconds(), refreshInterval) * 1000;
			range = 2 + Math.log(refreshInterval);
		}

		while (start < end) {
			if (isEnum) {
				EnumValue value = (EnumValue) module.getValue();
				List<Item> items = value.getEnumItems();

				int pos = 0;
				for (Item item : items) {
					if (item.getId() == (int) lastValue) {
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
	public List<Location> locations_getAll(String gateId) {
		return mLocations.getObjects(gateId);
	}

	@Override
	public boolean locations_update(Location location) {
		String gateId = location.getGateId();

		if (!mGates.hasObject(gateId)) {
			return false;
		}

		// NOTE: this replaces (or add) whole location
		mLocations.addObject(gateId, location);
		return true;
	}

	@Override
	public boolean locations_delete(Location location) {
		return mLocations.removeObject(location.getGateId(), location.getId()) != null;
	}

	@Override
	public Location locations_create(Location location) {
		String gateId = location.getGateId();

		if (!mGates.hasObject(gateId)) {
			return null;
		}

		// Create unique location id
		String locationId;
		int i = 0;
		do {
			locationId = String.valueOf(i++);
		} while (mLocations.hasObject(gateId, locationId));

		// Set new location id
		location.setId(locationId);

		mLocations.addObject(gateId, location);

		return location;
	}

	@Override
	public boolean gateusers_invite(String gateId, ArrayList<User> users) {
		for (User user : users) {
			if (!gateusers_invite(gateId, user)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean gateusers_invite(String gateId, User user) {
		if (!mGates.hasObject(gateId)) {
			return false;
		}

		// Create unique user id
		String userId;
		int i = 0;
		do {
			userId = String.valueOf(i++);
		} while (mUsers.hasObject(gateId, userId));

		// Set new user id
		user.setId(userId);

		// Set user name to his e-mail, because names must provide server (and we don't have any users here)
		user.setName(user.getEmail());

		mUsers.addObject(gateId, user);
		return true;
	}

	@Override
	public boolean gateusers_remove(String gateId, List<User> users) {
		for (User user : users) {
			if (!gateusers_remove(gateId, user)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean gateusers_remove(String gateId, User user) {
		// TODO: Actual implementation deletes gate, not account...
		if (user.getId().equals(mUser.getId())) {
			// If we're deleting ourselves, remove whole gate
			mLocations.removeHolder(gateId);
			mDevices.removeHolder(gateId);
			return mGates.removeObject(gateId) != null;
		} else {
			// TODO: This is correct implementation for future
			return mUsers.removeObject(gateId, user.getId()) != null;
		}
	}

	@Override
	public List<User> gateusers_getAll(String gateId) {
		return mUsers.getObjects(gateId);
	}

	@Override
	public boolean gateusers_updateAccess(String gateId, ArrayList<User> users) {
		for (User user : users) {
			if (!gateusers_updateAccess(gateId, user)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean gateusers_updateAccess(String gateId, User user) {
		if (!mGates.hasObject(gateId)) {
			return false;
		}

		// NOTE: this replaces (or add) whole user
		mUsers.addObject(gateId, user);

		// We can have only one superuser, so unset all other superusers of this gate (this does classic server too)
		if (user.getRole() == Role.Superuser) {
			for (User otherUser : mUsers.getObjects(gateId)) {
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

		// If we change role of ourselves, we need to put such change to gate itself too
		if (user.getId().equals(mUser.getId())) {
			Gate gate = mGates.getObject(gateId);
			gate.setRole(user.getRole());
		}

		return true;
	}

	@Override
	public boolean notifications_read(ArrayList<String> notificationIds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<VisibleNotification> notifications_getLatest() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

}
