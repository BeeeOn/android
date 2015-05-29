package com.rehivetech.beeeon.gamification.social;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.achievement.TwLoginAchievement;
import com.rehivetech.beeeon.util.Log;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiException;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthException;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.util.Observable;

import io.fabric.sdk.android.Fabric;

/**
 * Design pattern Singleton
 *
 * @author Jan Lamacz
 */
public class SocialTwitter extends Observable implements ISocialNetwork {
	private static final String TAG = SocialTwitter.class.getSimpleName();
	private static final String NAME = "Twitter";

	private static SocialTwitter mInstance;
	private SharedPreferences mPrefs;
	private Context mContext;
	private Twitter mTwitter;
	TwitterCore mCore;

	// Facebook user variables
	private String mUserName;
	private String mAccessToken;

	private SocialTwitter(Context context) {
		mContext = context;
		mPrefs = Controller.getInstance(mContext).getUserSettings();
		mAccessToken = mPrefs.getString(Constants.PERSISTENCE_PREF_LOGIN_TWITTER, null);
		TwitterAuthConfig twConfig =
				new TwitterAuthConfig(mContext.getString(R.string.twitter_app_id),
						mContext.getString(R.string.twitter_app_secret));
		mTwitter = new Twitter(twConfig);
		Fabric.with(mContext, mTwitter);
		mCore = mTwitter.core;
	}

	public static SocialTwitter getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SocialTwitter(context);
		}
		return mInstance;
	}

	@Override
	public void logIn(final Activity activity) {
		mCore.logIn(activity, new Callback<TwitterSession>() {
			@Override
			public void success(Result<TwitterSession> twitterSessionResult) {
				parseResult(twitterSessionResult);
				new TwLoginAchievement(activity);
				setChanged();
				notifyObservers("login");
			}

			@Override
			public void failure(TwitterException e) {
				if (e instanceof TwitterAuthException) {
					if (!e.getMessage().toLowerCase().contains("cancel")) {
						Toast.makeText(mContext, mContext.getString(R.string.social_no_connection), Toast.LENGTH_LONG).show();
						setChanged();
						notifyObservers("connect_error");
					}
				} else if (e instanceof TwitterApiException)
					Toast.makeText(mContext, "SSL is required", Toast.LENGTH_LONG).show();
				Log.e(TAG, "Login failed");
			}
		});
	}

	@Override
	public void logOut() {
		if (mUserName != null)
			Toast.makeText(mContext, mContext.getString(R.string.logout_success), Toast.LENGTH_LONG).show();
		mUserName = null;
//		Twitter.logOut();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getUserName() {
		return mUserName;
	}

	@Override
	public boolean isPaired() {
		return mAccessToken != null;
	}

	public void downloadUserData() {
	}

	/**
	 * Should be called startActivity(mTw.shareAchievement(--text--));
	 * Sharing text via twitter without need to have Twitter API, be logged on etc...
	 * If the Twitter app is installed, shares with this app, otherwise shares via web browser
	 * Downsides:
	 * cant check if tweet is truly done (etc for achievements)
	 * cant return back to app and stays in browser
	 */
	public Intent shareAchievement(String text) {
		return new TweetComposer.Builder(mContext)
				.text(text + " @beeeonapp")
				.createIntent();
	}

	private void parseResult(Result<TwitterSession> result) {
		mUserName = result.data.getUserName();
		mAccessToken = result.data.getAuthToken().token;
		Log.d(TAG, "Twitter token: " + mAccessToken);
		mPrefs.edit().putString(Constants.PERSISTENCE_PREF_LOGIN_TWITTER, mAccessToken).apply();
	}

}
