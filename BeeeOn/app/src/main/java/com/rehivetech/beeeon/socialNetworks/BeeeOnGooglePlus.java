package com.rehivetech.beeeon.socialNetworks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.plus.PlusShare;
import com.rehivetech.beeeon.controller.Controller;

/**
 * @author Jan Lamacz
 */
public class BeeeOnGooglePlus implements BeeeOnSocialNetwork {
	private static final String TAG = BeeeOnGooglePlus.class.getSimpleName();

	private static BeeeOnGooglePlus mInstance;
	private Context mContext;

	private BeeeOnGooglePlus(Context context) {
		mContext = context;
	}

	public static BeeeOnGooglePlus getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new BeeeOnGooglePlus(context);
		}
		return mInstance;
	}

	public Intent shareAchievement(String text) {
		return new PlusShare.Builder(mContext)
				.setType("text/plain")
				.setText(text+" @BeeeOn")
				.setContentUrl(Uri.parse("https://www.beeeon.com/"))
				.getIntent();
	}

	@Override
	public void logIn(FragmentActivity activity) {
	}
	@Override
	public void logOut() {
	}
	@Override
	public boolean isPaired() {return true;}
	@Override
	public String getUserName() {return Controller.getInstance(mContext).getActualUser().getName();}
}
