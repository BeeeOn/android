package com.rehivetech.beeeon.gcm.notification;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class VisibleNotification extends Notification {

	private String mMessage;

	/**
	 * Constructor
	 */
	public VisibleNotification(int userId, int msgid, long time, NotificationType type, boolean read, String message) {
		super(userId, msgid, time, type, read);
		mMessage = message;
	}
}
