package com.rehivetech.beeeon.household.device;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.OrderIdentifierComparator;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.SimpleDataHolder;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

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

	private String mLocationId;
	private boolean mInitialized;
	private DateTime mInvolveTime;
	private int mNetworkQuality;
	private DateTime mLastUpdate;

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
		List<Module> modules = type.createModules(this);
		// Sort them by order, id
		Collections.sort(modules, new OrderIdentifierComparator());

		// Then set the modules holder
		mModules.setObjects(modules);
	}

	/**
	 * Factory method for creating new Device objects.
	 *
	 * @param typeId
	 * @param gateId
	 * @param address
	 * @return
	 */
	public static final Device createDeviceByType(String typeId, String gateId, String address) {
		DeviceType type = Utils.getEnumFromId(DeviceType.class, typeId, DeviceType.TYPE_UNKNOWN);
		return new Device(type, gateId, address);

		//throw new IllegalArgumentException(String.format("Unknown device type: %d", typeId));
	}

	public boolean isUnknownType() {
		return mType == DeviceType.TYPE_UNKNOWN;
	}

	/**
	 * @return time of last update
	 */
	public DateTime getLastUpdate() {
		return mLastUpdate;
	}

	/**
	 * @param lastUpdate time of last update
	 */
	public void setLastUpdate(DateTime lastUpdate) {
		mLastUpdate = lastUpdate;
	}

	/**
	 * Check if actual value of this sensor is expired
	 *
	 * @return true when refresh interval since last update expired
	 */
	public boolean isExpired() {
		DeviceFeatures features = mType.getFeatures();
		if (!features.hasRefresh()) {
			return true;
		}
		return mLastUpdate.plusSeconds(features.getActualRefresh().getInterval()).isBeforeNow();
	}

	public String getId() {
		return mAddress;
	}

	public DeviceType getType() {
		return mType;
	}

	/**
	 * @return location id of device
	 */
	public String getLocationId() {
		return mLocationId;
	}

	/**
	 * Setting location of device
	 *
	 * @param locationId
	 */
	public void setLocationId(String locationId) {
		// From server we've got "", but internally we need to use Location.NO_LOCATION_ID
		if (locationId.isEmpty())
			locationId = Location.NO_LOCATION_ID;

		mLocationId = locationId;
	}

	/**
	 * Get gate id of device
	 *
	 * @return gate id
	 */
	public String getGateId() {
		return mGateId;
	}

	/**
	 * Returning flag if mDevice has been initialized yet
	 *
	 * @return
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * Setting flag for mDevice initialization state
	 *
	 * @param initialized
	 */
	public void setInitialized(boolean initialized) {
		mInitialized = initialized;
	}

	/**
	 * Get time of setting of device to system
	 *
	 * @return involve time
	 */
	public DateTime getInvolveTime() {
		return mInvolveTime;
	}

	/**
	 * @param pairedTime Time when device was paired to gate
	 */
	public void setPairedTime(DateTime pairedTime) {
		mPairedTime = pairedTime;
	}

	/**
	 * Get MAC address of device
	 *
	 * @return address
	 */
	public String getAddress() {
		return mAddress;
	}

	/**
	 * Get value of signal quality
	 *
	 * @return quality
	 */
	public int getNetworkQuality() {
		return mNetworkQuality;
	}

	/**
	 * Setting quality
	 *
	 * @param networkQuality
	 */
	public void setNetworkQuality(int networkQuality) {
		mNetworkQuality = networkQuality;
	}

	public List<Module> getModules() {
		return mModules.getObjects();
	}

	// TODO: Remove this method, ideally
	public Module getModuleByType(ModuleType type, int offset) {
		for (Module module : getModules()) {
			if (module.getType().equals(type) && module.getOffset() == offset) {
				return module;
			}
		}

		return null;
	}

	public Module getModuleById(String id) {
		return mModules.getObject(id);
	}

	public void setModuleValue(String id, String value) {
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
