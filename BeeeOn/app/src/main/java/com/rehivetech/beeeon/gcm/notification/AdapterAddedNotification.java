package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class AdapterAddedNotification extends VisibleNotification {


	private int mAdapterId;

	private AdapterAddedNotification(String userId, int msgid, long time, NotificationType type, boolean read, int adapterId) {
		super(userId, msgid, time, type, read);
		mAdapterId = adapterId;
	}

	public static AdapterAddedNotification getInstance(NotificationName name, Integer msgId, String userId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException{
		AdapterAddedNotification instance = null;

		try {
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));

			if ( adapterId == null  ) {
				Log.d(TAG, "Adapter added: some compulsory value is missing.");
				return null;
			}

			instance = new AdapterAddedNotification(userId, msgId, time, type, false, adapterId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	@Override
	protected void onGcmHandle(Context context) {
		// TODO notifikvoat controler aby si stahl nova data
	}

	@Override
	protected void onClickHandle(Context context) {
		// 	TODO
		Toast.makeText(context, "on click", Toast.LENGTH_LONG).show();
	}


	@Override
	protected String getMessage(Context context) {
		// TODO pridat lokalizovany string
		return "ahoj";
	}

	@Override
	protected String getName(Context context) {
		return context.getString(R.string.notification_name_new_adapter);
	}

}
