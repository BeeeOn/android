package com.rehivetech.beeeon.gcm;

import com.rehivetech.beeeon.gcm.notification.Notification;

public abstract interface INotificationReceiver {

	/**
	 * This can be called from any thread, receiver must make sure he handle it correctly (e.g. in Activity use runOnUiThread)
	 *
	 * @param notification
	 */
	public void receiveNotification(Notification notification);

}
