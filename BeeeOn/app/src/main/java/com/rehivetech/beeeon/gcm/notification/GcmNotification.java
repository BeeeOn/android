package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;

/**
 * Created by Martin on 26. 4. 2015.
 */
public interface GcmNotification {

	String getUserId();
	int getId();
	void onGcmRecieve(Context context);

	/**
	 * Enum mapping string name of notification to end class for instantiation
	 */
	public enum NotificationName {
		WATCHDOG("watchdog"),
		DELETE_NOTIF("delete_not"),
		URI("uri"),
		SENSOR_ADDED("sensor_add"),
		ADAPTER_ADDED("adapter_add");


		private String mName;

		NotificationName(String name) {
			mName = name;
		}

		public String getName() {
			return mName;
		}

		public static NotificationName fromValue(String value) {
			for (NotificationName item : values()) {
				if (value.equalsIgnoreCase(item.getName()))
					return item;
			}
			throw new IllegalArgumentException("Invalid State value");
		}
	}

	public enum NotificationType {
		INFO("info"),
		ADVERT("advert"),
		ALERT("alert"),
		CONTROL("control");

		private final String mValue;

		NotificationType(String value) {
			mValue = value;
		}

		public static NotificationType fromValue(String value) {
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
