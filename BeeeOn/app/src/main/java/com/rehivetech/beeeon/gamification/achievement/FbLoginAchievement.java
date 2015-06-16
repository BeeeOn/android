package com.rehivetech.beeeon.gamification.achievement;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;

/**
 * @author Jan Lamacz
 */
public class FbLoginAchievement extends Achievement {
	public FbLoginAchievement(Context context, String token) {
		super(Constants.ACHIEVEMENT_FACEBOOK_LOGIN, context, false);
		if (!mData.isDone()) {
			SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
			prefs.edit().putString(Constants.PERSISTENCE_PREF_LOGIN_FACEBOOK, token).apply();
		}
	}
}
