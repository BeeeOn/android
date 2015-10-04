package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;

/**
 * Created by Martin on 26. 4. 2015.
 */
public interface IGcmNotification {

	int getId();

	void onGcmRecieve(Context context);

	/**
	 * Enum mapping string name of notification to end class for instantiation
	 */
	enum NotificationName {
		DELETE_NOTIF("delete_not", DeleteNotification.class),
		URI("uri", UriNotification.class),
		DEVICE_ADDED("sensor_add", DeviceAddedNotification.class),
		DEVICE_LOW_BATTERY("sensor_bat", DeviceLowBatteryNotification.class),
		DEVICE_LOW_SIGNAL("sensor_sig", DeviceLowSignalNotification.class),
		GATE_ADDED("adapter_add", GateAddedNotification.class),
		GATE_OFFLINE("adapter_off", GateOfflineNotification.class);

		private final String mName;
		private final Class<? extends BaseNotification> mClass;

		NotificationName(String name, Class<? extends BaseNotification> baseClass) {
			mName = name;
			mClass = baseClass;
		}

		public static NotificationName fromValue(String value) {
			for (NotificationName item : values()) {
				if (value.equalsIgnoreCase(item.getName()))
					return item;
			}
			throw new IllegalArgumentException("Invalid State value");
		}

		public String getName() {
			return mName;
		}

		public Class<? extends BaseNotification> getBaseClass() {
			return mClass;
		}
	}

	enum NotificationType {
		INFO("info", 300),
		ADVERT("advert", 200),
		ALERT("alert", 400),
		CONTROL("control", 100);

		private final String mValue;
		private final int mLevel;

		NotificationType(String value, int level) {
			mValue = value;
			mLevel = level;
		}

		public static NotificationType fromValue(String value) throws IllegalArgumentException {
			for (NotificationType item : values()) {
				if (value.equalsIgnoreCase(item.getName()))
					return item;
			}
			throw new IllegalArgumentException("Invalid State value");
		}

		public static NotificationType fromValue(int value) throws IllegalArgumentException {
			for (NotificationType item : values()) {
				if (value == item.getLevel())
					return item;
			}
			throw new IllegalArgumentException("Invalid State value");
		}

		public String getName() {
			return mValue;
		}

		public int getLevel() {
			return mLevel;
		}
	}
}
