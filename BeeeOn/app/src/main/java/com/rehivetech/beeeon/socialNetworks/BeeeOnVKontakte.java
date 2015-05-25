package com.rehivetech.beeeon.socialNetworks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.achievements.GeneralAchievement;
import com.rehivetech.beeeon.achievements.VkLoginAchievement;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.dialogs.VKShareDialog;

import java.util.Observable;

/**
 * @author Jan Lamacz
 */
public class BeeeOnVKontakte extends Observable implements BeeeOnSocialNetwork{
	private static final String TAG = BeeeOnVKontakte.class.getSimpleName();
	private static final String NAME = "Vkontakte";

	private static BeeeOnVKontakte mInstance;
	private Context mContext;

	// user variables
	private String mUserName;
	private String mAccessToken;

	private BeeeOnVKontakte(Context context) {
		mContext = context.getApplicationContext();
		SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
		mAccessToken = prefs.getString(Constants.PERSISTENCE_PREF_LOGIN_VKONTAKTE, null);
	}

	public static BeeeOnVKontakte getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new BeeeOnVKontakte(context);
		}
		return mInstance;
	}

	@Override
	public boolean isPaired() {return mAccessToken != null;}
	@Override
	public String getName() {return NAME;}
	@Override
	public String getUserName() {return mUserName;}

	@Override
	public void logIn(Activity activity) {
		VKSdk.authorize(null, true, false);
		downloadUserData();
	}
	@Override
	public void logOut() {
		if(mUserName != null) {
			Toast.makeText(mContext, mContext.getString(R.string.logout_success), Toast.LENGTH_LONG).show();
			VKSdk.logout();
		}
		mUserName = null;
	}

	public VKShareDialog shareAchievement(String title, String date) {
		return new VKShareDialog()
			.setText(date + " " + mContext.getString(R.string.achievement_share_msg) + " #beeeon")
			.setAttachmentLink(mContext.getString(R.string.achievement_share_url_name),
					mContext.getString(R.string.achievement_share_url))
			.setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
				public void onVkShareComplete(int postId) {
					new GeneralAchievement(Constants.ACHIEVEMENT_VKONTAKTE_SHARE, mContext);
				}
				public void onVkShareCancel() {
					Log.d(TAG, "sharing canceled");
				}
			});
	}

	public void downloadUserData() {
		if(mUserName != null) {
			Log.d(TAG, "Not downloading data, already done before");
			return;
		}
		Log.d(TAG, "Trying to download user data");
		VKApi.users().get().executeWithListener(new VKRequest.VKRequestListener() {
			@Override
			public void onComplete(VKResponse response) {
				VKApiUser user = ((VKList<VKApiUser>) response.parsedModel).get(0);
				mUserName = user.first_name + " " + user.last_name;
				setChanged();
				notifyObservers("vkontakte");
			}
			@Override
			public void onError(VKError error) {
				if(error.errorCode == -105) {
					setChanged();
					notifyObservers("connect_error");
				}
			}
			@Override
			public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
				Log.d(TAG, "progress");
			}
			@Override
			public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
				Log.d(TAG, "fail");
			}
		});
	}

	public VKSdkListener getListener() {
		return new VKSdkListener() {
			@Override
			public void onCaptchaError(VKError vkError) {
				Log.d(TAG, "error");
			}
			@Override
			public void onTokenExpired(VKAccessToken vkAccessToken) {
				Log.d(TAG, "token expired");
			}
			@Override
			public void onAccessDenied(VKError vkError) {
				Log.d(TAG, "access denied");
			}
			@Override
			public void onReceiveNewToken(VKAccessToken newToken) {
				mAccessToken = newToken.toString();
				new VkLoginAchievement(mContext, mAccessToken);
				newToken.saveTokenToSharedPreferences(mContext, Constants.PERSISTENCE_PREF_LOGIN_VKONTAKTE);
				setChanged();
				notifyObservers("login");
			}
			@Override
			public void onAcceptUserToken(VKAccessToken token) {
				Log.d(TAG, "onacceptusertoken");
			}
		};
	}
}
