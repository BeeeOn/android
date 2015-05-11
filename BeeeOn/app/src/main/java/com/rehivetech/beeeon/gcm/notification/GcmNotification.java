package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;

/**
 * Created by Martin on 26. 4. 2015.
 */
public interface GcmNotification {

	int getId();

	void onGcmRecieve(Context context);

	/**
	 * Enum mapping string name of notification to end class for instantiation
	 */
	enum NotificationName {
		WATCHDOG("watchdog", WatchdogNotification.class),
		DELETE_NOTIF("delete_not", DeleteNotification.class),
		URI("uri", UriNotification.class),
		SENSOR_ADDED("sensor_add", SensorAddedNotification.class),
		SENSOR_LOW_BATTERY("sensor_bat", SensorLowBatteryNotification.class),
		SENSOR_LOW_SIGNAL("sensor_sig", SensorLowSignalNotification.class),
		ADAPTER_ADDED("adapter_add", AdapterAddedNotification.class),
		ADAPTER_OFFLINE("adapter_off", AdapterOfflineNotification.class),
		ACHIEVEMENT("achievement", AchievementNotification.class);

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
		INFO("info"),
		ADVERT("advert"),
		ALERT("alert"),
		CONTROL("control");

		private final String mValue;

		NotificationType(String value) {
			mValue = value;
		}

		public static NotificationType fromValue(String value) throws IllegalArgumentException {
			for (NotificationType item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid State value");
		}

		public String getValue() {
			return mValue;
		}
	}
}
