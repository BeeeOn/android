package com.rehivetech.beeeon.gamification.social;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.plus.PlusShare;
import com.rehivetech.beeeon.controller.Controller;

/**
 * @author Jan Lamacz
 */
public class SocialGooglePlus implements ISocialNetwork {
	private static final String TAG = SocialGooglePlus.class.getSimpleName();
	private static final String NAME = "Google Plus";

	private static SocialGooglePlus mInstance;
	private Context mContext;

	private SocialGooglePlus(Context context) {
		mContext = context;
	}

	public static SocialGooglePlus getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SocialGooglePlus(context);
		}
		return mInstance;
	}

	public Intent shareAchievement(String text) {
		return new PlusShare.Builder(mContext)
				.setType("text/plain")
				.setText(text + " @BeeeOn")
				.setContentUrl(Uri.parse("https://www.beeeon.com/"))
				.getIntent();
	}

	@Override
	public void logIn(Activity activity) {
	}

	@Override
	public void logOut() {
	}

	@Override
	public boolean isPaired() {
		return true;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getUserName() {
		return Controller.getInstance(mContext).getActualUser().getName();
	}
}
