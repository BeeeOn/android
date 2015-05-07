package com.rehivetech.beeeon.achievements;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.login.LoginResult;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.socialNetworks.BeeeOnFacebook;

/**
 * @author Jan Lamacz
 */
public class FbLoginAchievement extends Achievement {
	private static final String TAG = FbLoginAchievement.class.getSimpleName();

	public FbLoginAchievement(Context context, LoginResult loginResult) {
		super(Constants.ACHIEVEMENT_FACEBOOK_LOGIN, context);
		if(!mData.isDone()) {
			Controller controller = Controller.getInstance(context);
			SharedPreferences prefs = controller.getUserSettings();
			String token = loginResult.getAccessToken().toString();
			BeeeOnFacebook.getInstance(context).setToken(token);
			prefs.edit().putString(Constants.PERSISTENCE_PREF_LOGIN_FACEBOOK,token).apply();
		}
	}
}
