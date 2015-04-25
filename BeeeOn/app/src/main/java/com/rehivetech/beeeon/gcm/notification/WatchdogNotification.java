package com.rehivetech.beeeon.gcm.notification;

import android.os.Bundle;

import com.rehivetech.beeeon.network.xml.Xconstants;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class WatchdogNotification extends VisibleNotification {



	public static WatchdogNotification getInstance(NotificationName name, Integer msgId, Integer userId, Long time, NotificationType type, Bundle bundle) {
		WatchdogNotification instance = null;

		try {
			String message = bundle.getString(Xconstants.MESSAGE);
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));
			String deviceId = bundle.getString(Xconstants.DID);
			Integer deviceType = Integer.valueOf(bundle.getString("dtype"));

			if (message == null || adapterId == null || deviceId == null || deviceType == null) {
				return null;
			}

			instance = new WatchdogNotification(userId, msgId, time, type, false, message, adapterId, deviceId, deviceType);
		} catch (IllegalArgumentException|NullPointerException e) {
			return instance;
		}

		return instance;
	}

	private int mAdapterId;
	private String mSensorId;
	private int mSensorType;

	/**
	 * Constructor
	 *
	 * @param userId
	 * @param msgid
	 * @param time
	 * @param type
	 * @param read
	 */
	private WatchdogNotification(int userId, int msgid, long time, NotificationType type, boolean read, String message, int adapterId, String sensorId, int sensorType) {
		super(userId, msgid, time, type, read, message);
		mAdapterId = adapterId;
		mSensorId = sensorId;
		mSensorType = sensorType;
	}
}
