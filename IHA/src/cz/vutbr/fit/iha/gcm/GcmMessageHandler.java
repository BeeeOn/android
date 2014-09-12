package cz.vutbr.fit.iha.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LoginActivity;
import cz.vutbr.fit.iha.controller.Controller;

public class GcmMessageHandler extends IntentService {

	private Handler handler;

	public GcmMessageHandler() {
		super("GcmMessageHandler");
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		handler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (extras == null || extras.isEmpty() || messageType == null || messageType.isEmpty()) {
			GcmBroadcastReceiver.completeWakefulIntent(intent);
			Log.e(GcmHelper.TAG_GCM, "Null notification");
			return;
		}

		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			Log.w(GcmHelper.TAG_GCM, "Send error: " + extras.toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
			Log.w(GcmHelper.TAG_GCM, "Deleted messages on server: " + extras.toString());
			// If it's a regular GCM message, do some work.
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			Notification notification = Notification.parseBundle(extras);

			// control if message was valid
			if (notification == null) {
				Log.e(GcmHelper.TAG_GCM, "Invalid message. Some of compulsory values was missing.");
				GcmBroadcastReceiver.completeWakefulIntent(intent);
				return;
			}

			if (!notification.getEmail().equals(Controller.getInstance(getApplicationContext()).getLastEmail())) {
				// TODO poslat na server delete GCM ID
				Log.w(GcmHelper.TAG_GCM, "Notification email wasn't veryfied. Server GCM ID will be deleted.");
				Log.w(GcmHelper.TAG_GCM, notification.getEmail() + " != " + Controller.getInstance(this).getLastEmail());
			}
			// EVERYTHING VERYFIED SUCCESFULY, MAKE ACTION HERE
			else {
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher_white).setContentTitle(getText(R.string.app_name))
						.setContentText(notification.getMessage()).setAutoCancel(true);

				// define notification action
				Intent resultIntent = new Intent(this, LoginActivity.class);

				// Because clicking the notification opens a new ("special") activity, there's
				// no need to create an artificial back stack.
				PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				// Set the Notification's Click Behavior
				mBuilder.setContentIntent(resultPendingIntent);

				// Gets an instance of the NotificationManager service
				NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

				// FIXME msgid se musi parsovat jako int
				// Builds the notification and issues it.
				mNotifyMgr.notify(Integer.valueOf(notification.getMsgid()), mBuilder.build());

				// showToast(notification.getMessage());
				Log.i(GcmHelper.TAG_GCM, "Received : (" + messageType + ")  " + notification.getMessage());
			}
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	public void showToast(final String message) {
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			}
		});

	}
}
