package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;

/**
 * Created by Martin on 26. 4. 2015.
 */
public abstract class VisibleNotification extends BaseNotification {

	public VisibleNotification(String userId, int msgid, long time, NotificationType type, boolean read) {
		super(userId, msgid, time, type, read);
	}

	abstract protected String getMessage();
}
