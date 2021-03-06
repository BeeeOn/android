package com.rehivetech.beeeon.gcm.notification;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.NotificationActivity;
import com.rehivetech.beeeon.gui.adapter.NotificationAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by Martin on 26. 4. 2015.
 */
public abstract class VisibleNotification extends BaseNotification {

	public VisibleNotification(int msgid, long time, NotificationType type, boolean read) {
		super(msgid, time, type, read);
	}

	@Nullable
	private static VisibleNotification getInstance(NotificationName name, NotificationType type, Integer msgId, Long time, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException {
		VisibleNotification notification = null;
		switch (name) {
			case GATE_ADDED:
				notification = GateAddedNotification.getInstance(msgId, time, type, isRead, parser);
				break;
			case DEVICE_ADDED:
				notification = DeviceAddedNotification.getInstance(msgId, time, type, isRead, parser);
				break;
			case URI:
				notification = UriNotification.getInstance(msgId, time, type, isRead, parser);
				break;
			case DEVICE_LOW_BATTERY:
				notification = DeviceLowBatteryNotification.getInstance(msgId, time, type, isRead, parser);
				break;
			case DEVICE_LOW_SIGNAL:
				notification = DeviceLowSignalNotification.getInstance(msgId, time, type, isRead, parser);
				break;
			case GATE_OFFLINE:
				notification = GateOfflineNotification.getInstance(msgId, time, type, isRead, parser);
				break;
		}
		return notification;
	}

	@Nullable
	public static VisibleNotification parseXml(String nameStr, String idStr, String timeStr, String typeStr, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException {
		try {
			NotificationName name = NotificationName.fromValue(nameStr);
			NotificationType type = NotificationType.fromValue(Integer.valueOf(typeStr));
			Integer id = Integer.valueOf(idStr);
			Long time = Long.valueOf(timeStr);

			return getInstance(name, type, id, time, isRead, parser);
		} catch (IllegalArgumentException e) {
			Timber.e("Some value couldn't be parsed from String value. Returning null. %s", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	abstract protected String getMessage(Context context);

	abstract protected String getName(Context context);

	abstract protected void onClickHandle(Context context);

	@SuppressLint("NewApi")
	protected NotificationCompat.Builder getBaseNotificationBuilder(Context context) {
		final Intent resultIntent = new Intent(context, NotificationActivity.class);
		resultIntent.putExtras(getBundle());
//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//		// Adds the back stack
//		stackBuilder.addParentStack(NotificationActivity.class);
//		// Adds the Intent to the top of the stack
//		stackBuilder.addNextIntent(resultIntent);
//		// Gets a PendingIntent containing the entire back stack
//		PendingIntent resultPendingIntent =
//				stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		PendingIntent resultPendingIntent;
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			// Creates the PendingIntent
			resultPendingIntent =
					PendingIntent.getActivity(
							context,
							0,
							resultIntent,
							PendingIntent.FLAG_UPDATE_CURRENT
					);
		} else {
			final Intent backIntent = new Intent(context, MainActivity.class);
			backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

			resultPendingIntent = PendingIntent.getActivities(context, 0,
					new Intent[]{backIntent, resultIntent}, PendingIntent.FLAG_ONE_SHOT);
		}


		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.beeeon_logo_white_border)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.beeeon_logo_white_border))
				.setWhen(getDate().getTimeInMillis())
				.setContentTitle(context.getText(R.string.app_name))
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				.setPriority(getType() == NotificationType.ALERT ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_DEFAULT)
				.setContentText(getMessage(context))
				.setContentIntent(resultPendingIntent)
				.setAutoCancel(true);

		return builder;
	}

	public void setView(Context context, NotificationAdapter.ViewHolder holder) {
		DateTime dateTime = new DateTime(getDate().getTimeInMillis());
		boolean isTooOld = dateTime.plusHours(23).isBeforeNow();
		DateTimeFormatter fmt = isTooOld ? DateTimeFormat.shortDate() : DateTimeFormat.mediumTime();

		holder.text.setText(getMessage(context));
		holder.name.setText(getName(context));
		holder.time.setText(fmt.print(getDate().getTimeInMillis()));
		holder.img.setImageResource(getImageRes());

		if (isRead()) {
			holder.text.setTypeface(null, Typeface.NORMAL);
			holder.name.setTypeface(null, Typeface.NORMAL);
		} else {
			holder.text.setTypeface(null, Typeface.BOLD);
			holder.name.setTypeface(null, Typeface.BOLD);
		}
	}

	public void onClick(final Context context) {
		if (!isRead()) {
			setRead(true);

			final Controller controller = Controller.getInstance(context);
			Thread t = new Thread() {
				public void run() {
					controller.getGcmModel().setNotificationRead(String.valueOf(VisibleNotification.this.getId()));
				}
			};
			t.start();
		}
		onClickHandle(context);
	}

	private int getImageRes() {
		switch (getType()) {
			case ADVERT:
				return R.drawable.ic_notif_pr_gray;
			case ALERT:
				return R.drawable.ic_notif_alert_gray;
			// INFO
			default:
				return R.drawable.ic_notif_info_gray;
		}
	}
}
