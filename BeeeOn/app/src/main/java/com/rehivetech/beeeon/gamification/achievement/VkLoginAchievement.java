package com.rehivetech.beeeon.gamification.achievement;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;

/**
 * @author Jan Lamacz
 */
public class VkLoginAchievement extends Achievement {
	public VkLoginAchievement(Context context, String token) {
		super(Constants.ACHIEVEMENT_VKONTAKTE_LOGIN, context, false);
		if (!mData.isDone()) {
			SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
			prefs.edit().putString(Constants.PERSISTENCE_PREF_LOGIN_VKONTAKTE, token).apply();
		}
	}
}
