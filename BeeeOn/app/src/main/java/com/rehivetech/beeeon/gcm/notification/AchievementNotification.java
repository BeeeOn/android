package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AchievementOverviewActivity;
import com.rehivetech.beeeon.activity.NotificationActivity;
import com.rehivetech.beeeon.activity.ProfileDetailActivity;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 *
 */
public class AchievementNotification extends VisibleNotification {
	public static final String TAG = AchievementNotification.class.getSimpleName();

	private long mTime;
	private String mAchievementID = null;
	private AchievementList mList = null;

	private AchievementNotification(int msgid, long time, NotificationType type, boolean read, String achievementID) {
		super(msgid, time, type, read);
		mAchievementID = achievementID;
		mTime = time;
	}

	protected static AchievementNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		AchievementNotification instance;

		try {
			String achievementId = bundle.getString(Xconstants.ID);

			if (achievementId == null) {
				Log.d(TAG, "Achievement: some compulsory value is missing.");
				return null;
			}

			instance = new AchievementNotification(msgId, time, type, false, achievementId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}

		return instance;
	}

	@Nullable
	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		String achievementId = null;

		if (parser == null) {
			Log.e(TAG, "Parser is NULL");
			return null;
		}

		if (parser.getName() == null) {
			Log.e(TAG, "Parser.getName() is NULL");
			return null;
		}

		String text = null;
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.END_TAG &&
					parser.getName().equals(Xconstants.NOTIFICATION)) {
				break;
			}
			String tagname = parser.getName();
			switch (eventType) {
				case XmlPullParser.START_TAG:
					// ignore it
					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if (tagname.equalsIgnoreCase(Xconstants.ID)) {
						achievementId = text;
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (achievementId == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new AchievementNotification(msgId, time, type, isRead, achievementId);
	}

	@Override
	protected void onGcmHandle(Context context) {
		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);

		// Downloading data from server
		mList = AchievementList.getInstance(context);

		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context) {
		Intent intent;

		mList = AchievementList.getInstance(context);
		if(mList.isDownloaded() && mAchievementID != null) {
			AchievementListItem achievementItem = mList.getItem(String.valueOf(mAchievementID));
			if(achievementItem != null) {
				achievementItem.setCompleted(mTime);
				mList.updateData();
				Bundle bundle = new Bundle();
				bundle.putString(AchievementOverviewActivity.EXTRA_CATEGORY_ID, achievementItem.getCategory());

				intent = new Intent(context, AchievementOverviewActivity.class);
				intent.putExtras(bundle);
			}
			else
				intent = new Intent(context, NotificationActivity.class);
		}
		// Known ID, but somehow couldnt download achievement data
		else if(mAchievementID != null) {
			intent = new Intent(context, ProfileDetailActivity.class);
		}
		// Unknown ID
		else {
			intent = new Intent(context, NotificationActivity.class);
		}
		context.startActivity(intent);
	}

	@Override
	protected String getMessage(Context context) {
		return context.getString(R.string.toast_achievement_title);
	}

	@Override
	protected String getName(Context context) {
		if(mAchievementID != null)
			return context.getString(context.getResources().getIdentifier("name_" + mAchievementID, "string", context.getPackageName()));
		return "New achievement";
	}
}
