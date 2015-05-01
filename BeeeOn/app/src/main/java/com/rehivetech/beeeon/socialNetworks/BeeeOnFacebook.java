package com.rehivetech.beeeon.socialNetworks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.achievements.FbLoginAchievement;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Observable;

/**
 * Design pattern Singleton
 * Design pattern Observer
 * @author Jan Lamacz
 */
public class BeeeOnFacebook extends Observable implements BeeeOnSocialNetwork{
	private static final String TAG = BeeeOnFacebook.class.getSimpleName();
	private static final String NAME = "Facebook";

	private static BeeeOnFacebook mInstance;
	private Context mContext;

	// Facebook user variables
	private String mUserName;
	private String mAccessToken;

	private BeeeOnFacebook(Context context) {
		mContext = context;
		SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
		mAccessToken = prefs.getString(Constants.PERSISTENCE_PREF_LOGIN_FACEBOOK, null);
	}

	public static BeeeOnFacebook getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new BeeeOnFacebook(context);
		}
		return mInstance;
	}

	public void setToken(String token) {this.mAccessToken = token;}

	@Override
	public String getName() {return NAME;}
	@Override
	public String getUserName() {return mUserName;}
	@Override
	public boolean isPaired() {return mAccessToken != null;}

	@Override
	public void logOut() {
		if(mUserName != null) {
			Toast.makeText(mContext, mContext.getString(R.string.logout_success), Toast.LENGTH_LONG).show();
			LoginManager.getInstance().logOut();
		}
		mUserName = null;
	}

	@Override
	public void logIn(Activity activity) {
		LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile"));
	}

	public ShareLinkContent shareAchievement(String title, String date) {
		if (ShareDialog.canShow(ShareLinkContent.class)) {
			return new ShareLinkContent.Builder()
					.setContentTitle(title)
					.setContentDescription(date + " " + mContext.getString(R.string.achievement_share_msg))
					.setContentUrl(Uri.parse(mContext.getString(R.string.achievement_share_url)))
					.setImageUrl(Uri.parse(mContext.getString(R.string.achievement_share_img)))
					.build();
		}
		return null;
	}

	public FacebookCallback getListener() {
		return new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				new FbLoginAchievement(mContext,loginResult);
				setChanged();
				notifyObservers("facebook login");
//				mProfileFrag.updateFacebookLoginView();
			}
			@Override
			public void onCancel() {}
			@Override
			public void onError(FacebookException exception) {}
		};
	}

	public void downloadUserData() {
		if(mUserName != null) {
			Log.d(TAG, "Not downloading data, already done before");
			return;
		}
		AccessToken token = AccessToken.getCurrentAccessToken();
		GraphRequest request = GraphRequest.newMeRequest(token,new GraphRequest.GraphJSONObjectCallback() {
			@Override
			public void onCompleted(JSONObject object, GraphResponse response) {
				if(response.getError() != null) {
					setChanged();
					if(response.getError().getErrorCode() == -1) {
//						Toast.makeText(mContext, mContext.getString(R.string.NetworkError___CL_INTERNET_CONNECTION), Toast.LENGTH_SHORT).show();
						notifyObservers("connect_error");
					}
					else
						notifyObservers("not_logged");
				}
				else {
					try {
						mUserName = object.getString("name");
						setChanged();
						notifyObservers("facebook");
					}
					catch(JSONException e) {
						Log.e(TAG, "FB JSON parse error: "+e.getMessage());
					}
				}
			}
		});
		request.executeAsync();
	}
}
