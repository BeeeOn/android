package com.rehivetech.beeeon.gcm.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.NotificationActivity;
import com.rehivetech.beeeon.arrayadapter.NotificationAdapter;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.TimeHelper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by Martin on 26. 4. 2015.
 */
public abstract class VisibleNotification extends BaseNotification {

	public VisibleNotification(String userId, int msgid, long time, NotificationType type, boolean read) {
		super(userId, msgid, time, type, read);
	}

	abstract protected String getMessage(Context context);
	protected abstract String getName(Context context);

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

		final Intent backIntent = new Intent(context, MainActivity.class);
		backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

		final PendingIntent resultPendingIntent = PendingIntent.getActivities(context, 0,
				new Intent[] {backIntent, resultIntent}, PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.beeeon_logo_white)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.beeeon_logo_white_icons))
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
		boolean isTooOld =  dateTime.plusHours(23).isBeforeNow();
		DateTimeFormatter fmt = isTooOld ? DateTimeFormat.shortDate() : DateTimeFormat.mediumTime();

		holder.text.setText(getMessage(context));
		holder.name.setText(getName(context));
		holder.time.setText(fmt.print(getDate().getTimeInMillis()));
		holder.img.setImageResource(getImageRes());

		if (isRead()) {
			holder.text.setTypeface(null, Typeface.NORMAL);
			holder.text.setTypeface(null, Typeface.NORMAL);
		} else {
			holder.text.setTypeface(null, Typeface.BOLD);
			holder.text.setTypeface(null, Typeface.BOLD);
		}
	}

	abstract protected void onClickHandle(Context context);

	public void onClick(final Context context) {
		if (isRead()) {
			setRead(true);
			final Controller controller = Controller.getInstance(context);
			Thread t = new Thread() {
				public void run() {
					controller.getGcmModel().setNotificationRead(String.valueOf(getId()));
				}
			};
			t.start();
		}
		onClickHandle(context);
	}

	private int getImageRes() {
		// FIXME spravne obrazky
		switch (getType()) {
			case ADVERT:
				return R.drawable.dev_unknown;
			case ALERT:
				return R.drawable.dev_unknown;
			case CONTROL:
				return R.drawable.dev_unknown;
			// INFO
			default:
				return R.drawable.dev_unknown;
		}
	}
}
