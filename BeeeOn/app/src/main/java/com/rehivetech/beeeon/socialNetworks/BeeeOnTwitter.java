package com.rehivetech.beeeon.socialNetworks;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.achievements.FbLoginAchievement;
import com.rehivetech.beeeon.achievements.TwLoginAchievement;
import com.rehivetech.beeeon.activity.fragment.ProfileDetailFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiException;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthException;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;

import io.fabric.sdk.android.Fabric;

/**
 * Design pattern Singleton
 * @author Jan Lamacz
 */
public class BeeeOnTwitter {
	private static final String TAG = BeeeOnTwitter.class.getSimpleName();

	private static BeeeOnTwitter mInstance;
	private SharedPreferences mPrefs;
	private Context mContext;
	private Twitter mTwitter;

	// Facebook user variables
	private String mUserName;
	private String mAccessToken;

	private BeeeOnTwitter(Context context) {
		mContext = context;
		mPrefs = Controller.getInstance(mContext).getUserSettings();
//		mAccessToken = mPrefs.getString(Constants.PERSISTANCE_PREF_LOGIN_TWITTER, null);
//		TwitterAuthConfig twConfig =
//				new TwitterAuthConfig(mContext.getString(R.string.twitter_app_id),
//						mContext.getString(R.string.twitter_app_secret));
//		mTwitter = new Twitter(twConfig);
//		Fabric.with(mContext, mTwitter);
	}

	public static BeeeOnTwitter getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new BeeeOnTwitter(context);
		}
		return mInstance;
	}

	public void logIn(final FragmentActivity activity) {
		Twitter.logIn(activity, new Callback<TwitterSession>() {
			@Override
			public void success(Result<TwitterSession> twitterSessionResult) {
				downloadUserData(twitterSessionResult);
				new TwLoginAchievement(activity.getApplicationContext());
			}
			@Override
			public void failure(TwitterException e) {
				if(e instanceof TwitterAuthException)
					Toast.makeText(mContext, mContext.getString(R.string.NetworkError___NO_CONNECTION), Toast.LENGTH_LONG).show();
				Log.e(TAG, "Login failed");
			}
		});
	}

	public void downloadUserData(Result<TwitterSession> result) {
		mUserName = result.data.getUserName();
		mAccessToken = result.data.getAuthToken().token;
		mPrefs.edit().putString(Constants.PERSISTANCE_PREF_LOGIN_TWITTER, mAccessToken);
	}

	public String getUserName() {return mUserName;}
	public boolean isPaired() {return mAccessToken != null;}
}
