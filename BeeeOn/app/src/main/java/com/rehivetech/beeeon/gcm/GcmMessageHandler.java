package com.rehivetech.beeeon.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.notification.BaseNotification;
import com.rehivetech.beeeon.gcm.notification.Notification;
import com.rehivetech.beeeon.util.Log;

public class GcmMessageHandler extends IntentService {

	public static final String TAG = GcmMessageHandler.class.getSimpleName();

	private Handler mHandler;
	private Controller mController;

	public GcmMessageHandler() {
		super("GcmMessageHandler");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
		mController = Controller.getInstance(this);
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
			Log.w(TAG, GcmHelper.TAG_GCM + "Null notification");
			return;
		}

		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			Log.w(TAG, GcmHelper.TAG_GCM + "Send error: " + extras.toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
			Log.w(TAG, GcmHelper.TAG_GCM + "Deleted messages on server: " + extras.toString());
			// If it's a regular GCM message, do some work.
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			handleNotification(intent);
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void handleNotification(Intent intent) {
		final Notification notification = BaseNotification.parseBundle(mController, intent.getExtras());

		// control if message was valid
		if (notification == null) {
			Log.e(TAG, GcmHelper.TAG_GCM + "Invalid message.");
			GcmBroadcastReceiver.completeWakefulIntent(intent);
			return;
		}

		notification.handle(this, mController);
	}

//BACKDOOR, AFTER DEMONIGHT DELETE THIS METHOD
//	private void createXmasNotification(String msg) {
//		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//				.setSmallIcon(R.drawable.beeeon_logo_white)
//				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.beeeon_logo_white_icons))
//				// .setWhen(notification.getDate().getTimeInMillis())
//				.setWhen(System.currentTimeMillis()).setContentTitle(getText(R.string.app_name))
//				.setContentText(msg).setAutoCancel(true);
//
//		// vibration
//		builder.setVibrate(new long[] {
//			500, 500, 500, 500, 500, 500, 500
//		});
//
//		// LED
//		builder.setLights(Color.RED, 3000, 3000);
//
//		// sound
//		Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.xmas);
//		if (uri == null) {
//			Log.e(GcmHelper.TAG_GCM, "MP3 URI is null");
//		}
//		builder.setSound(uri);
//
//		// define notification action
//		Intent resultIntent = new Intent(this, LoginActivity.class);
//
//		// Because clicking the notification opens a new ("special") activity, there's
//		// no need to create an artificial back stack.
//		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//
//		// Set the Notification's Click Behavior
//		builder.setContentIntent(resultPendingIntent);
//
//		// Gets an instance of the NotificationManager service
//		NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//		// Builds the notification and issues it.
//		mNotifyMgr.notify(9999, builder.build());
//
//
//
//		// showToast(notification.getMessage());
//	}
}