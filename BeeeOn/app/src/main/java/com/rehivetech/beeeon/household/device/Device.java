package com.rehivetech.beeeon.household.device;

import android.support.annotation.Nullable;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.OrderIdentifierComparator;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.SimpleDataHolder;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public final class Device implements IIdentifier {
	public static final String TAG = Device.class.getSimpleName();

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

	private String mLocationId;
	private boolean mInitialized;
	private DateTime mPairedTime;
	private int mNetworkQuality;
	private DateTime mLastUpdate;

	private RefreshInterval mRefreshInterval;
	private int mBatteryValue;

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
	public static final Device createDeviceByType(String typeId, String gateId, String address) {
		DeviceType type = Utils.getEnumFromId(DeviceType.class, typeId, DeviceType.TYPE_UNKNOWN);
		return new Device(type, gateId, address);
	}

	/**
	 * @return True if this device has unknown type, false otherwise.
	 */
	public boolean isUnknownType() {
		return mType == DeviceType.TYPE_UNKNOWN;
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
		if (!mType.getFeatures().hasRefresh()) {
			return true;
		}
		return mLastUpdate.plusSeconds(getRefresh().getInterval()).isBeforeNow();
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
	 * @return True if this device has been initialized already, false otherwise.
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * @param initialized State of initialization of this device.
	 */
	public void setInitialized(boolean initialized) {
		mInitialized = initialized;
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
	 * @return quality of network signal in percents (0-100%)
	 */
	public int getNetworkQuality() {
		return mNetworkQuality;
	}

	/**
	 * @param networkQuality quality of network signal in percents (0-100%)
	 */
	public void setNetworkQuality(int networkQuality) {
		mNetworkQuality = networkQuality;
	}

	/**
	 * @return Actual RefreshInterval value, or null if this device doesn't have refresh at all.
	 */
	@Nullable
	public RefreshInterval getRefresh() {
		if (!mType.getFeatures().hasRefresh()) {
			return null;
		}
		return mRefreshInterval != null ? mRefreshInterval : mType.getFeatures().getDefaultRefresh();
	}

	/**
	 * @param refreshInterval New RefreshInterval value, which could be null to reset it to default value.
	 */
	public void setRefresh(@Nullable RefreshInterval refreshInterval) {
		if (!mType.getFeatures().hasRefresh()) {
			Log.w(TAG, "Trying to set refresh value, but this Device doesn't have refresh.");
			return;
		}
		mRefreshInterval = refreshInterval;
	}

	/**
	 * @return Actual battery value (0-100%) or -1 when this device has no battery.
	 */
	public int getBattery() {
		return mType.getFeatures().hasBattery() ? mBatteryValue : -1;
	}

	/**
	 * @param batteryValue New battery level value.
	 */
	public void setBattery(int batteryValue) {
		if (!mType.getFeatures().hasBattery()) {
			Log.w(TAG, "Trying to set refresh value, but this Device doesn't have battery.");
			return;
		}
		mBatteryValue = batteryValue;
	}

	/**
	 * @return List of modules this device contains.
	 */
	public List<Module> getAllModules() {
		List<Module> modules = mModules.getObjects();

		// Sort modules by proper order
		Collections.sort(modules, new OrderIdentifierComparator());

		return modules;
	}

	/**
	 * @return List of actually visible modules this device contains.
	 */
	public List<Module> getVisibleModules() {
		// This will give us correctly sorted modules
		List<Module> modules = getAllModules();

		List<String> hideModuleIds = new ArrayList<>();

		// Get info from all the modules about what modules they propose to hide based on their own value
		for (Module module : modules) {
			hideModuleIds.addAll(module.getHideModuleIdsFromRules());
		}

		// Remove modules that should be hidden
		Iterator<Module> it = modules.iterator();
		while (it.hasNext()) {
			Module module = it.next();
			if (hideModuleIds.contains(module.getId())) {
				it.remove();
			}
		}

		return modules;
	}

	/**
	 * @param id
	 * @return Module from this device with specified id.
	 */
	public Module getModuleById(String id) {
		return mModules.getObject(id);
	}

	/**
	 * Set value of module from this device, specified by id.
	 * If module with specified id doesn't exists, for Devices with unknown type is new unknown module automatically created and its value set.
	 *
	 * @param id
	 * @param value
	 * @throws IllegalStateException When module with specified id doesn't exists and this device has not unknown type.
	 */
	public void setModuleValue(String id, String value) throws IllegalStateException {
		synchronized (mModules) {
			if (!mModules.hasObject(id)) {
				if (!isUnknownType()) {
					// TODO: have it here this way? It could be possible to support such circumstances without a crash - but on other hand, such situation shouldn't happen when everything is regard our specifications.
					throw new IllegalStateException(String.format("Module #%s doesn't exists in this device type #%s. Only unknown devices can set values of unspecified modules.", id, mType.getId()));
				}

				// Automatically create new unknown module for this device
				mModules.addObject(Module.createUnknownModule(this, id));
			}

			// At this moment module will surely exist in data holder
			Module module = mModules.getObject(id);
			module.setValue(value);
		}
	}

	/**
	 * Data holder used for saving device data to server.
	 */
	public static class DataPair {
		public final Device mDevice;
		public final EnumSet<Module.SaveModule> what;
		public final Location location;

		public DataPair(final Device device, final EnumSet<Module.SaveModule> what) {
			this(device, null, what);
		}

		public DataPair(final Device device, final Location newLoc, final EnumSet<Module.SaveModule> what) {
			this.mDevice = device;
			this.what = what;
			this.location = newLoc;
		}
	}
}
