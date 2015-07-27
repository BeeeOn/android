/**
 *
 */
package com.rehivetech.beeeon.gcm.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

import java.util.Calendar;

public abstract class BaseNotification implements IGcmNotification, Comparable<BaseNotification> {

	public static final String TAG = BaseNotification.class.getSimpleName();

	private static final String SEPARATOR = "\\s+";
	private final Calendar mDate;
	private final int mId;
	private final NotificationType mType;
	private boolean mRead = false;

	private Bundle mBundle;

	/**
	 * Constructor
	 */
	public BaseNotification(int msgid, long timestamp, NotificationType type, boolean read) {
		/**
		 * Convert UTC timezone to local timezone
		 */
		mDate = Calendar.getInstance();
		mDate.setTimeInMillis(timestamp);

		mType = type;
		mRead = read;
		mId = msgid;
	}

	public static IGcmNotification parseBundle(Context context, Bundle bundle) {
		if (bundle == null) {
			return null;
		}

		Controller controller = Controller.getInstance(context);

		Log.d(TAG, bundle.toString());
		BaseNotification notification = null;
		try {
			NotificationName name = NotificationName.fromValue(bundle.getString(Xconstants.NOTIFICATION_NAME));
			Integer msgId = Integer.valueOf(bundle.getString(Xconstants.MSGID));
			String userId = bundle.getString(Xconstants.UID);
			Long time = Long.valueOf(bundle.getString(Xconstants.TIME));
			NotificationType type = NotificationType.fromValue(bundle.getString(Xconstants.TYPE));

			// control validity of message
			if (name == null || msgId == null || userId == null || time == null || type == null) {
				Log.w(TAG, "Some of compulsory values is missing");
				return null;
			}
			// control if actual user ID is the samesdsdas
			if (!userId.equals(controller.getActualUser().getId())) {
				Log.w(TAG, "GCM: Sent user ID is different from actual user ID. Deleting GCM on server. Actual ID: " + controller.getActualUser().getId() + ", recieved ID: " + userId);
				controller.getGcmModel().deleteGCM(userId, null);
				return null;
			}

			notification = getInstance(name, msgId, time, type, bundle);
		}
		// catch nullpointer if some of bundle values doesn't exist
		// catch IllegalArgumentException if cannot cast
		catch (NullPointerException | IllegalArgumentException e) {
			Log.w(TAG, "Nullpointer or cannot parse to enum/number: " + e.getLocalizedMessage());

			return null;
		}
		if (notification != null) {
			notification.setBundle(bundle);
		}
		return notification;
	}

	@Nullable
	private static BaseNotification getInstance(NotificationName name, Integer msgId, Long time,
												NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		BaseNotification notification = null;

		switch (name) {
			case WATCHDOG:
				notification = WatchdogNotification.getInstance(msgId, time, type, bundle);
				break;
			case GATE_ADDED:
				notification = GateAddedNotification.getInstance(msgId, time, type, bundle);
				break;
			case DEVICE_ADDED:
				notification = DeviceAddedNotification.getInstance(msgId, time, type, bundle);
				break;
			case DELETE_NOTIF:
				notification = DeleteNotification.getInstance(msgId, time, type, bundle);
				break;
			case URI:
				notification = UriNotification.getInstance(msgId, time, type, bundle);
				break;
			case DEVICE_LOW_BATTERY:
				notification = DeviceLowBatteryNotification.getInstance(msgId, time, type, bundle);
				break;
			case DEVICE_LOW_SIGNAL:
				notification = DeviceLowSignalNotification.getInstance(msgId, time, type, bundle);
				break;
			case GATE_OFFLINE:
				notification = GateOfflineNotification.getInstance(msgId, time, type, bundle);
				break;
		}

		return notification;
	}

	protected void setBundle(Bundle bundle) {
		mBundle = bundle;
	}

	protected Bundle getBundle() {
		return mBundle;
	}

	abstract protected void onGcmHandle(Context context);


	public void onGcmRecieve(final Context context) {
		final Controller controller = Controller.getInstance(context);

		if (context == null || controller == null) {
			Log.e(TAG, "onGcmRecieve(): context or controller is NULL");
			return;
		}

		// if somebody already handle notification using controller observer, then do nothing
		if (passToController(controller)) {
			return;
		}

		onGcmHandle(context);
	}

	/**
	 * Send notification to controller.
	 *
	 * @param controller Actual controller
	 * @return True if notification was handled by controller. False otherwise.
	 */
	protected boolean passToController(Controller controller) {
		return controller.getGcmModel().receiveNotification(this);
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
	 * @return True if notification is visible for user. False otherwise.
	 */
	public boolean isVisible() {
		return getType() != NotificationType.CONTROL;
	}


	protected void showNotification(Context context, NotificationCompat.Builder builder) {
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Builds the notification and issues it.
		mNotifyMgr.notify(getId(), builder.build());
	}

	@Override
	public int compareTo(BaseNotification notification) {
		if (getDate().after(notification.getDate())) {
			return -1;
		} else if (getDate().before(notification.getDate())) {
			return 1;
		} else {
			return 0;
		}
	}
}
