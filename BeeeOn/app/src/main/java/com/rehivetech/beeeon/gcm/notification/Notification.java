/**
 *
 */
package com.rehivetech.beeeon.gcm.notification;

import android.os.Bundle;

import com.rehivetech.beeeon.network.xml.Xconstants;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;

public abstract class Notification {

	public static final String TAG = Notification.class.getSimpleName();
	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String SEPARATOR = "\\s+";
	private final Calendar mDate;
	private final int mId;
	private final NotificationType mType;
	private DateTimeFormatter mFormatter = DateTimeFormat.forPattern(DATEFORMAT).withZoneUTC();
	private boolean mRead = false;
	private String mUserId = null;

	/**
	 * Constructor
	 */
	public Notification(int userId, int msgid, long time, NotificationType type, boolean read) {
		mId = msgid;

		/** FIXME opravit timezone */
		mDate = Calendar.getInstance();
		mDate.setTimeInMillis(time);

		mType = type;
		mRead = read;
	}

	public static Notification parseBundle(Bundle bundle) {
		Notification notification = null;
		try {
			NotificationName name = NotificationName.fromValue(bundle.getString(bundle.getString(Xconstants.NOTIFICATION_NAME)));
			Integer msgId = Integer.valueOf(bundle.getString(Xconstants.MSGID));
			Integer userId = Integer.valueOf(bundle.getString(Xconstants.UID));
			Long time = Long.valueOf(bundle.getString(Xconstants.TIME));
			NotificationType type = NotificationType.fromValue(bundle.getString(Xconstants.TYPE));

			// control validity of message
			if (name == null || msgId == null || userId == null || time == null || type == null) {
				return null;
			}

			notification = getInstance(name, msgId, userId, time, type, bundle);
		}
		// catch nullpointer if some of bundle values doesn't exist
		// catch IllegalArgumentException if cannot cast
		catch (NullPointerException | IllegalArgumentException e) {
			return null;
		}

		return notification;
	}

	private static Notification getInstance(NotificationName name, Integer msgId, Integer userId, Long time,
											NotificationType type, Bundle bundle) {
		Notification notification = null;

		switch (name) {
			case WATCHDOG:
				notification = WatchdogNotification.getInstance(name, msgId, userId, time, type, bundle);
				break;
		}

		return notification;
	}

	/**
	 * @return Email if notification was received by GCM. Null otherwise.
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * @return the mDate
	 */
	public Calendar getDate() {
		return mDate;
	}

	/**
	 * @return the notification ID
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @return If notification was already read or not.
	 */
	public boolean isRead() {
		return mRead;
	}

	/**
	 * @param read the mRead to set
	 */
	public void setRead(boolean read) {
		this.mRead = read;
	}

	/**
	 * @return Notification type (info, advert, alert, control)
	 */
	public NotificationType getType() {
		return mType;
	}

	/**
	 * @return If notification is visible for user.
	 */
	public boolean isVisible() {
		return getType() != NotificationType.CONTROL;
	}

	/**
	 * Enum mapping string name of notification to end class for instantiation
	 */
	public enum NotificationName {
		WATCHDOG("watchdog");

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
