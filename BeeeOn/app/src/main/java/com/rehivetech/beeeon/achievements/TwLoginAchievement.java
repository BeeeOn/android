package com.rehivetech.beeeon.achievements;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;

/**
 * @author Jan Lamacz
 */
public class TwLoginAchievement extends Achievement {
	private static final String TAG = TwLoginAchievement.class.getSimpleName();

	public TwLoginAchievement(Context context) {
		super(Constants.ACHIEVEMENT_TWITTER_LOGIN, context);
		if(!mData.isDone()) {
			Controller controller = Controller.getInstance(context);
			SharedPreferences prefs = controller.getUserSettings();
			prefs.edit().putString(Constants.PERSISTENCE_PREF_LOGIN_FACEBOOK,"whatever").apply();
		}
	}
}
