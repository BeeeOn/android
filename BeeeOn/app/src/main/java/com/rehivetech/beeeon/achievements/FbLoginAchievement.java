package com.rehivetech.beeeon.achievements;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.login.LoginResult;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;

/**
 * @author Jan Lamacz
 */
public class FbLoginAchievement extends Achievement {
	public FbLoginAchievement(Context context, LoginResult loginResult) {
		super("3", context);
		if(!mData.isDone()) {
			Controller controller = Controller.getInstance(context);
			SharedPreferences prefs = controller.getUserSettings();
			prefs.edit().putString(Constants.PERSISTANCE_PREF_LOGIN_FACEBOOK,loginResult.getAccessToken().toString()).apply();
			//TODO network
			show();
		}
	}
}
