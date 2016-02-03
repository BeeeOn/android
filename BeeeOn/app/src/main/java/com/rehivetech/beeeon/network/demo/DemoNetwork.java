package com.rehivetech.beeeon.network.demo;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Gender;
import com.rehivetech.beeeon.household.user.User.Role;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.util.DataHolder;
import com.rehivetech.beeeon.util.GpsData;
import com.rehivetech.beeeon.util.MultipleDataHolder;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.util.ValuesGenerator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
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

	private final Context mContext;
	private final User mUser;
	private String mBT;
	private boolean mInitialized;
	private final Map<String, Random> mRandoms = new HashMap<>();

	private final DataHolder<GateInfo> mGates = new DataHolder<>();
	private final MultipleDataHolder<Location> mLocations = new MultipleDataHolder<>();
	private final MultipleDataHolder<Device> mDevices = new MultipleDataHolder<>();
	private final MultipleDataHolder<User> mUsers = new MultipleDataHolder<>();

	public DemoNetwork(Context context) {
		mContext = context;

		String demoModeName = mContext.getString(R.string.demo_network_demo_mode);
		String demoModeEmail = mContext.getString(R.string.demo_network_demo_mode_email);

		// Set user
		mUser = new User(DEMO_USER_ID, demoModeName, "", demoModeEmail, Gender.UNKNOWN, Role.Owner);

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
				int id = Utils.parseIntSafely(gateId, 0);
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

		String newValue = ValuesGenerator.generateValue(module, rand);
		module.setValue(newValue);

		// Set error status randomly (but rarely)
		module.getDevice().setStatus(rand.nextInt() % 10 != 0 ? Device.STATUS_AVAILABLE : Device.STATUS_UNAVAILABLE);
	}

	public void initDemoData() throws AppException {
		if (mInitialized)
			return;

		// Erase previous data if exists
		mGates.clear();
		mLocations.clear();
		mDevices.clear();
		mUsers.clear();
		mRandoms.clear();

		DemoData demoData = new DemoData(mUser);
		mGates.setObjects(demoData.getGates(mContext));
		for (GateInfo gate : mGates.getObjects()) {
			String gateId = gate.getId();

			mLocations.setObjects(gateId, demoData.getLocation(mContext, gateId));
			mLocations.setLastUpdate(gateId, DateTime.now());

			mDevices.setObjects(gateId, demoData.getDevices(gateId));
			mDevices.setLastUpdate(gateId, DateTime.now());

			mUsers.setObjects(gateId, demoData.getUsers(gateId));
			mUsers.setLastUpdate(gateId, DateTime.now());

			Random rand = getRandomForGate(gate.getId());

			// Set last update time to time between (-26 hours, now>
			for (Device device : mDevices.getObjects(gateId)) {
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
		mBT = "";
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
		// FIXME: Implement this
		throw new AppException(NetworkError.UNDER_DEVELOPMENT);
	}

	@Override
	public boolean accounts_disconnectAuthProvider(String providerName) {
		// FIXME: Implement this
		throw new AppException(NetworkError.UNDER_DEVELOPMENT);
	}

	@Override
	public boolean gates_register(String gateId, String gateName, int offsetInMinutes) {
		if (!isGateAllowed(gateId)) {
			return false;
		}

		GateInfo gate = new GateInfo(gateId, gateName);
		gate.setUtcOffset(offsetInMinutes);
		gate.setRole(Role.Owner);

		mGates.addObject(gate);
		mUsers.addObject(gateId, mUser);

		return true;
	}

	@Override
	public List<Gate> gates_getAll() {
		// Init demo data, if not initialized yet
		initDemoData();

		List<Gate> gates = new ArrayList<>();
		for (GateInfo gateInfo : mGates.getObjects()) {
			gates.add(new Gate(gateInfo.getId(), gateInfo.getName()));
		}
		return gates;
	}

	@Override
	public GateInfo gates_get(String gateId) {
		GateInfo gate = mGates.getObject(gateId);
		if (gate == null)
			return null;

		gate.setDevicesCount(mDevices.getObjects(gateId).size());
		gate.setUsersCount(mUsers.getObjects(gateId).size());

		return gate;
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
	public boolean gates_update(Gate gate, GpsData gpsData) {
		String gateId = gate.getId();

		GateInfo oldGate = mGates.getObject(gateId);
		if (oldGate == null)
			return false;

		oldGate.setName(gate.getName());
		oldGate.setUtcOffset(gate.getUtcOffset());
		oldGate.setGpsData(gpsData);
		return true;
	}

	@Override
	public boolean gates_unregister(String gateId) {
		mLocations.removeHolder(gateId);
		mDevices.removeHolder(gateId);
		mUsers.removeHolder(gateId);
		mRandoms.remove(gateId);
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
	public boolean gates_search(String gateId, String deviceIpAddress) {
		return isGateAllowed(gateId); // TODO I guess that is what should be
	}

	@Override
	public boolean devices_unregister(Device device) {
		return mDevices.removeObject(device.getGateId(), device.getId()) != null;
	}

	@Override
	public boolean devices_createParameter(Device device, String key, String value) {
		Random rand = getRandomForGate(device.getGateId());
		return rand.nextBoolean();
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
		// Create unique device id
		String address;
		do {
			address = gateId + "/" + String.valueOf(rand.nextInt());
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
		// device.setLocationId(locationId); // uninitialized device has no location
		device.setNetworkQuality(rand.nextInt(101));
		device.setStatus(Device.STATUS_AVAILABLE);

		// Set random values for device modules
		for (Module module : device.getAllModules(true)) {
			setNewValue(module);
		}

		// Add to list that we return
		newDevices.add(device);
		return newDevices;
	}

	@Override
	public ModuleLog devices_getLog(String gateId, Module module, ModuleLog.DataPair pair) {
		// Generate random values for log in demo mode
		Random rand = getRandomForGate(gateId);

		return ValuesGenerator.generateLog(module, pair, rand);
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
		if (user.getId().equals(mUser.getId())) {
			// We can't remove self user
			throw new AppException(NetworkError.CANT_DO_THIS);
		}

		return mUsers.removeObject(gateId, user.getId()) != null;
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
		if (user.getRole() == Role.Owner) {
			for (User otherUser : mUsers.getObjects(gateId)) {
				// Change only other users, except me
				if (!otherUser.getId().equals(user.getId())) {
					continue;
				}

				// If their role is superuser, switch them to just admin
				if (otherUser.getRole() == Role.Owner) {
					otherUser.setRole(Role.Admin);
				}
			}
		}

		// If we change role of ourselves, we need to put such change to gate itself too
		if (user.getId().equals(mUser.getId())) {
			GateInfo gate = mGates.getObject(gateId);
			gate.setRole(user.getRole());
		}

		return true;
	}

	@Override
	public boolean notifications_read(ArrayList<String> notificationIds) {
		// FIXME: Implement this
		throw new AppException(NetworkError.UNDER_DEVELOPMENT);
	}

	@Override
	public List<VisibleNotification> notifications_getLatest() {
		// FIXME: Implement this
		throw new AppException(NetworkError.UNDER_DEVELOPMENT);
	}

}
