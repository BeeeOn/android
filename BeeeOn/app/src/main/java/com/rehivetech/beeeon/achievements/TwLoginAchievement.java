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
		super("3", context);
		if(!mData.isDone()) {
			Controller controller = Controller.getInstance(context);
			SharedPreferences prefs = controller.getUserSettings();
			prefs.edit().putString(Constants.PERSISTANCE_PREF_LOGIN_FACEBOOK,"whatever").apply();
			//TODO network
			show();
		}
	}
}
