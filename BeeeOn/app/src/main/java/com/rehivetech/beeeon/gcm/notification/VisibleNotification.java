package com.rehivetech.beeeon.gcm.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.NotificationActivity;

/**
 * Created by Martin on 26. 4. 2015.
 */
public abstract class VisibleNotification extends BaseNotification {

	public VisibleNotification(String userId, int msgid, long time, NotificationType type, boolean read) {
		super(userId, msgid, time, type, read);
	}

	abstract protected String getMessage();

	protected NotificationCompat.Builder getBaseNotificationBuilder(Context context) {
		// define notification action
		Intent resultIntent = new Intent(context, NotificationActivity.class);

		// set the same bundle as we recieve to be able to parse the same message in NotificationActivity
		resultIntent.putExtras(getBundle());

		/*
		 * The stack builder object will contain an artificial back stack for the
		 * started Activity.
 		 * This ensures that navigating backward from the Activity leads out of
         * your application to the Home screen.
         */
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);

		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);

		// Create pending intent
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
				0,
				PendingIntent.FLAG_UPDATE_CURRENT
		);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.beeeon_logo_white)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.beeeon_logo_white_icons))
						// .setWhen(notification.getDate().getTimeInMillis())
				.setWhen(getDate().getTimeInMillis())
				.setContentTitle(context.getText(R.string.app_name))
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				.setPriority(getType() == NotificationType.ALERT ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_DEFAULT)
				.setContentText(getMessage())
				.setContentIntent(resultPendingIntent)
				.setAutoCancel(true);

		return builder;
	}
}
