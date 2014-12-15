package cz.vutbr.fit.iha.base;

import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cz.vutbr.fit.iha.activity.LoginActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.gcm.INotificationReceiver;
import cz.vutbr.fit.iha.gcm.Notification;
import cz.vutbr.fit.iha.thread.ToastMessageThread;

/**
 * Abstract parent for activities that requires logged in user
 * 
 * When user is not logged in, it will switch to LoginActivity automatically.
 */
public abstract class BaseApplicationActivity extends BaseActivity implements INotificationReceiver {

	private boolean triedLoginAlready = false;

	protected boolean isPaused = false;

	@Override
	public void onResume() {
		super.onResume();

		Controller controller = Controller.getInstance(getApplicationContext());

		if (!controller.isLoggedIn()) {
			if (!triedLoginAlready) {
				triedLoginAlready = true;
				redirectToLogin(this);
			} else {
				finish();
			}
			return;
		} else {
			triedLoginAlready = false;
		}

		controller.registerNotificationReceiver(this);

		isPaused = false;
		onAppResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		Controller controller = Controller.getInstance(getApplicationContext());
		controller.unregisterNotificationReceiver(this);

		isPaused = true;
		onAppPause();
	}

	public static void redirectToLogin(Context context) {
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(LoginActivity.BUNDLE_REDIRECT, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(intent);
	}

	/**
	 * This is called after onResume(), but only when user is correctly logged in
	 */
	protected abstract void onAppResume();

	/**
	 * This is called after onPause()
	 */
	protected abstract void onAppPause();

	/**
	 * Method that receives Notifications.
	 */
	public void receiveNotification(Notification notification) {
		// FIXME: Leo (or someone else?) should implement correct handling of notifications (showing somewhere in activity or something like that?)
		(new ToastMessageThread(this, notification.getMessage())).start();
	}

	/**
	 * Start thread that creates new notifications at random interval. FIXME: this is just for testing and should be removed soon
	 */
	/*
	public void testingNotifications() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d("BaseApplicationActivity", "Starting thread for dummy notification");
				Controller controller = Controller.getInstance(BaseApplicationActivity.this);
				Random random = new Random();

				for (int i = 1; i <= 10; i++) {
					int secs = 5 + random.nextInt(10);
					try {
						Thread.sleep(secs * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

						// break();
					}

					Log.d("BaseApplicationActivity", "Creating new dummy notification");

					Notification notification = new Notification(String.valueOf(i), "2014-10-22 12:12:12", "info", false);
					// we need email in notification to check validity
					notification.setEmail("john@doe.com");
					notification.setMessage(String.format("J� jsem testovac� notifikace �.%d!", i));
					// notification.setAction(notification.new Action(action));

					controller.receiveNotification(notification);
				}

				Log.d("BaseApplicationActivity", "Stopped thread for dummy notification");
			}
		}).start();
	}
	*/

}
