package com.rehivetech.beeeon.household.device;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.OrderIdentifierComparator;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.SimpleDataHolder;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

public final class Device implements IIdentifier {

	/**
	 * Properties inherited from device's specification table.
	 */
	private final DeviceType mType;
	private final SimpleDataHolder<Module> mModules = new SimpleDataHolder<>();

	/**
	 * Properties belonging to real device.
	 */
	private final String mAddress;
	private final String mGateId;

	private String mLocationId = Location.NO_LOCATION_ID;
	private DateTime mPairedTime;
	private DateTime mLastUpdate;
	private String mCustomName = "";
	private
	@Status
	String mStatus = Status.AVAILABLE;

	/**
	 * Private constructor, Device objects are created by static factory method {@link Device#createDeviceByType(String, String, String)}}.
	 *
	 * @param type
	 * @param gateId
	 * @param address
	 */
	private Device(DeviceType type, String gateId, String address) {
		mType = type;
		mGateId = gateId;
		mAddress = address;

		// Create modules list
		mModules.setObjects(type.createModules(this));
	}

	/**
	 * Factory method for creating new Device objects.
	 *
	 * @param typeId
	 * @param gateId
	 * @param address
	 * @return Properly initialized new instance of Device.
	 */
	public static Device createDeviceByType(String typeId, String gateId, String address) {
		return createDevice(DeviceType.getById(typeId), gateId, address);
	}

	public static Device createDevice(DeviceType type, String gateId, String address) {
		return new Device(type, gateId, address);
	}

	/**
	 * @return True if this device has unknown type, false otherwise.
	 */
	public boolean isUnknownType() {
		return mType == DeviceType.getUnknown();
	}

	/**
	 * @return Time of last update.
	 */
	@Nullable
	public DateTime getLastUpdate() {
		return mLastUpdate;
	}

	/**
	 * @param lastUpdate Time of last update.
	 */
	public void setLastUpdate(@Nullable DateTime lastUpdate) {
		mLastUpdate = lastUpdate;
	}

	/**
	 * @return True when refresh interval since last update has expired, false otherwise.
	 */
	public boolean isExpired() {
		RefreshInterval refresh = getRefresh();
		if (refresh == null || mLastUpdate == null) {
			return true;
		}
		return mLastUpdate.plusSeconds(refresh.getInterval()).isBeforeNow();
	}

	/**
	 * @return Unique identifier of this device. Currently is used device address.
	 */
	public String getId() {
		return mAddress;
	}

	/**
	 * @return Object representing type of this device from specification.
	 */
	public DeviceType getType() {
		return mType;
	}

	/**
	 * @return Id of location where this device is placed.
	 */
	public String getLocationId() {
		return mLocationId;
	}

	/**
	 * @param locationId Id of location where this device is placed.
	 */
	public void setLocationId(String locationId) {
		// From server we've got "", but internally we need to use Location.NO_LOCATION_ID
		if (locationId.isEmpty())
			locationId = Location.NO_LOCATION_ID;

		mLocationId = locationId;
	}

	/**
	 * @return Id of parent gate of this device.
	 */
	public String getGateId() {
		return mGateId;
	}

	/**
	 * Return name of this device to be used for showing in GUI.
	 *
	 * @param context
	 * @return User configured name (if set), or default device name from definition.
	 * @see #setCustomName(String)
	 */
	@NonNull
	public String getName(Context context) {
		if (mCustomName.isEmpty())
			return getType().getName(context);

		return mCustomName;
	}

	/**
	 * @return User configured name (if set), or empty string.
	 */
	@NonNull
	public String getCustomName() {
		return mCustomName;
	}

	/**
	 * @param name User configured name, or empty string to use name from definition.
	 * @see #getName(Context)
	 * @see #getCustomName()
	 */
	public void setCustomName(@NonNull String name) {
		mCustomName = name;
	}

	public
	@Status
	String getStatus() {
		return mStatus;
	}

	public void setStatus(@Status String status) {
		mStatus = status;
	}

	/**
	 * @return Time when device was paired to gate
	 */
	public DateTime getPairedTime() {
		return mPairedTime;
	}

	/**
	 * @param pairedTime Time when device was paired to gate
	 */
	public void setPairedTime(DateTime pairedTime) {
		mPairedTime = pairedTime;
	}

	/**
	 * @return unique (mac) address of this device
	 */
	public String getAddress() {
		return mAddress;
	}

	/**
	 * @param withSpecial Whether to return all modules even with special (refresh/battery/signal/...) modules
	 * @return List of modules this device contains.
	 */
	public List<Module> getAllModules(boolean withSpecial) {
		List<Module> modules = mModules.getObjects();

		if (!withSpecial) {
			// Remove special modules
			Iterator<Module> it = modules.iterator();
			while (it.hasNext()) {
				Module module = it.next();
				if (module.getType().isSpecial()) {
					it.remove();
				}
			}
		}

		// Sort modules by proper order
		Collections.sort(modules, new OrderIdentifierComparator());

		return modules;
	}

	/**
	 * @return List of actually visible modules this device contains (without features modules).
	 */
	public List<Module> getVisibleModules(boolean withoutUnavailable) {
		// This will give us correctly sorted modules
		List<Module> modules = getAllModules(false);

		List<String> hideModuleIds = new ArrayList<>();

		// Get info from all the modules about what modules they propose to hide based on their own value
		for (Module module : modules) {
			hideModuleIds.addAll(module.getHideModuleIdsFromRules());
		}

		// Remove modules that should be hidden
		Iterator<Module> it = modules.iterator();
		while (it.hasNext()) {
			Module module = it.next();
			// Remove modules to be hidden based on rules or if module is unavailable
			if (hideModuleIds.contains(module.getId()) || (withoutUnavailable && module.getStatus().equals(Status.UNAVAILABLE))) {
				it.remove();
			}
		}

		return modules;
	}

	/**
	 * @param id
	 * @return Module from this device with specified id. Or NULL if no such module exists.
	 */
	public Module getModuleById(String id) {
		return mModules.getObject(id);
	}

	/**
	 * @param typeId
	 * @return List of modules from this device with specified type. Or empty list if no such module exists.
	 */
	public List<Module> getModulesByType(int typeId) {
		List<Module> modules = new ArrayList<>();

		for (Module module : getAllModules(true)) {
			if (module.getType().getTypeId() == typeId) {
				modules.add(module);
			}
		}

		return modules;
	}

	/**
	 * Set value of module from this device, specified by id.
	 * If module with specified id doesn't exists, for Devices with unknown type is new unknown module automatically created and its value set.
	 * For unexpected module is just Logged error.
	 *
	 * @param id
	 * @param value
	 * @param moduleStatus
	 */
	public void setModuleValue(String id, String value, String moduleStatus) throws IllegalStateException {
		synchronized (mModules) {
			if (!mModules.hasObject(id)) {
				if (!isUnknownType()) {
					// Log error of unexpected module
					Timber.e("Module #%s doesn't exists in this device type #%s. Only unknown devices can set values of unspecified modules.", id, mType.getId());
					return;
				}

				// Automatically create new unknown module for this device
				mModules.addObject(Module.createUnknownModule(this, id));
			}

			// At this moment module will surely exist in data holder
			Module module = mModules.getObject(id);
			module.setValue(value);
			module.setStatus(moduleStatus.isEmpty() ? Status.AVAILABLE : moduleStatus);
		}
	}

	@Nullable
	public Module getFirstModuleByType(ModuleType type) {
		List<Module> modules = getModulesByType(type.getTypeId());
		if (modules.isEmpty())
			return null;

		return modules.get(0);
	}

	/**
	 * TODO: Only temporary method. Should be rewrited better (and more efficiently).
	 *
	 * @return
	 */
	@Nullable
	public RefreshInterval getRefresh() {
		Module refresh = getFirstModuleByType(ModuleType.TYPE_REFRESH);
		if (refresh == null)
			return null;

		// TODO: Support getting default value automatically somehow in the BaseValue object?
		BaseValue value = refresh.getValue();
		double seconds = value.hasValue() ? value.getDoubleValue() : Utils.parseDoubleSafely(value.getDefaultValue(), Double.NaN);
		return RefreshInterval.fromInterval((int) seconds);
	}

	/**
	 * TODO: Only temporary method. Should be rewrited better (and more efficiently).
	 *
	 * @return
	 */
	@Nullable
	public Integer getBattery() {
		Module module = getFirstModuleByType(ModuleType.TYPE_BATTERY);
		return (module == null) ? null : (int) module.getValue().getDoubleValue();
	}

	/**
	 * TODO: Only temporary method. Should be rewrited better (and more efficiently).
	 *
	 * @return
	 */
	@Nullable
	public Integer getRssi() {
		Module module = getFirstModuleByType(ModuleType.TYPE_RSSI);
		return (module == null) ? null : (int) module.getValue().getDoubleValue();
	}

	/**
	 * TODO: Only temporary method. Should be rewrited better (and more efficiently).
	 *
	 * @param refresh
	 */
	public void setRefresh(RefreshInterval refresh) {
		Module module = getFirstModuleByType(ModuleType.TYPE_REFRESH);
		if (module != null)
			module.setValue(String.valueOf(refresh.getInterval()));
	}

	/**
	 * TODO: Only temporary method. Should be rewrited better (and more efficiently).
	 *
	 * @param battery
	 */
	public void setBattery(int battery) {
		Module module = getFirstModuleByType(ModuleType.TYPE_BATTERY);
		if (module != null)
			module.setValue(String.valueOf(battery));
	}

	/**
	 * TODO: Only temporary method. Should be rewrited better (and more efficiently).
	 *
	 * @param quality
	 */
	public void setNetworkQuality(int quality) {
		Module module = getFirstModuleByType(ModuleType.TYPE_RSSI);
		if (module != null)
			module.setValue(String.valueOf(quality));
	}


	public ArrayList<String> getModulesGroups(Context context) {
		return getModulesGroups(context, false);
	}

	/**
	 * Get all modules groups
	 *
	 * @param context              context to get string resource
	 * @param withoutHiddenModules if true, unavailable module will NOT be shown
	 * @return list of group names
	 */
	public ArrayList<String> getModulesGroups(Context context, boolean withoutHiddenModules) {
		ArrayList<String> moduleGroups = new ArrayList<>();
		List<Module> modules = getVisibleModules(withoutHiddenModules);

		for (Module module : modules) {
			String groupName = module.getGroupName(context);
			if (!moduleGroups.contains(groupName)) {
				moduleGroups.add(groupName);
			}
		}

		return moduleGroups;
	}

	/**
	 * Get all modules by group name
	 * @param context context to get string resource
	 * @param groupName group name
	 * @return list of modules
	 */
	public List<Module> getModulesByGroupName(Context context, String groupName, boolean withoutHiddenModules) {
		List<Module> modules = getVisibleModules(withoutHiddenModules);
		Iterator<Module> iterator = modules.iterator();
		while (iterator.hasNext()) {
			Module module = iterator.next();
			if (!module.getGroupName(context).equals(groupName)) {
				iterator.remove();
			}
		}
		return modules;
	}

	/**
	 * Data holder used for saving device data to server.
	 */
	public static class DataPair {
		public final Device device;
		public final Location location;
		public final boolean initializing;

		public DataPair(final Device device, final Location location, final boolean initializing) {
			this.device = device;
			this.location = location;
			this.initializing = initializing;
		}
	}
}
