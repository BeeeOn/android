package com.rehivetech.beeeon.gcm;

import com.rehivetech.beeeon.gcm.notification.GcmNotification;

public interface INotificationReceiver {

	/**
	 * This can be called from any thread, receiver must make sure he handle it correctly (e.g. in Activity use runOnUiThread)
	 *
	 * @param notification
	 */
	boolean receiveNotification(GcmNotification notification);

}
