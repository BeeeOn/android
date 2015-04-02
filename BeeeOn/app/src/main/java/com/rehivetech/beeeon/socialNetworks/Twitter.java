package com.rehivetech.beeeon.socialNetworks;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;

/**
 * Design pattern Singleton
 * @author Jan Lamacz
 */
public class Twitter {
	private static final String TAG = Twitter.class.getSimpleName();

	private static Twitter mInstance;
	private Context mContext;

	// Facebook user variables
	private String mUserName;
	private String mAccessToken;

	private Twitter(Context context) {
		mContext = context;
		SharedPreferences mPrefs = Controller.getInstance(mContext).getUserSettings();
		mAccessToken = mPrefs.getString(Constants.PERSISTANCE_PREF_LOGIN_TWITTER, null);
	}

	public static Twitter getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new Twitter(context);
		}
		return mInstance;
	}

	public String getUserName() {return mUserName;}
	public boolean isPaired() {
//		return true;
		return mAccessToken != null;
	}
}
