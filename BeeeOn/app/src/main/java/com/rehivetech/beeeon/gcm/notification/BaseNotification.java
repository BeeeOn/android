/**
 *
 */
package com.rehivetech.beeeon.gcm.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;

public abstract class BaseNotification implements Notification {

	public static final String TAG = BaseNotification.class.getSimpleName();
	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String SEPARATOR = "\\s+";
	private final Calendar mDate;
	private final int mId;
	private final NotificationType mType;
	private DateTimeFormatter mFormatter = DateTimeFormat.forPattern(DATEFORMAT).withZoneUTC();
	private boolean mRead = false;
	private String mUserId = null;

	private Bundle mBundle;

	/**
	 * Constructor
	 */
	public BaseNotification(String userId, int msgid, long time, NotificationType type, boolean read) {
		mId = msgid;

		/** FIXME opravit timezone */
		mDate = Calendar.getInstance();
		mDate.setTimeInMillis(time);

		mUserId = userId;
		mType = type;
		mRead = read;
	}

	public static Notification parseBundle(Controller controller, Bundle bundle) {
		if (bundle == null) {
			return null;
		}

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
			// control if actual user ID is the same
			if (!userId.equals(controller.getActualUser().getId())) {
				Log.w(TAG, "GCM: Sent user ID is different from actaul user ID. Deleting GCM on server.");
				controller.getGcmModel().deleteGCM(notification.getUserId(), null);
				return null;
			}

			notification = getInstance(name, msgId, userId, time, type, bundle);
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

	protected void setBundle(Bundle bundle) {
		mBundle = bundle;
	}

	private static BaseNotification getInstance(NotificationName name, Integer msgId, String userId, Long time,
											NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		BaseNotification notification = null;

		switch (name) {
			case WATCHDOG:
				notification = WatchdogNotification.getInstance(name, msgId, userId, time, type, bundle);
				break;
		}

		return notification;
	}

	abstract protected void onHandle(Context context, Controller controller);

	public void handle(Context context, Controller controller) {
		// if somebody already handle notification using controller observer, then do nothing
		if (passToController(controller)) {
			return;
		}

		onHandle(context, controller);
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

	protected NotificationCompat.Builder getBaseNotificationBuilder(Context context) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.beeeon_logo_white)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.beeeon_logo_white_icons))
						// .setWhen(notification.getDate().getTimeInMillis())
				.setWhen(System.currentTimeMillis())
				.setContentTitle(context.getText(R.string.app_name))
				.setAutoCancel(true);

		// vibration
		builder.setVibrate(new long[]{
				500
		});

		// LED
		builder.setLights(Color.BLUE, 3000, 3000);

		// sound
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		builder.setSound(uri);

		return builder;
	}

	protected void showNotification(Context context, NotificationCompat.Builder builder) {
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Builds the notification and issues it.
		mNotifyMgr.notify(getId(), builder.build());
	}
}
