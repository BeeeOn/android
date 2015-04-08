package com.rehivetech.beeeon.socialNetworks;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
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
public class BeeeOnFacebook extends Observable {
	private static final String TAG = BeeeOnFacebook.class.getSimpleName();

	private static BeeeOnFacebook mInstance;
	private Context mContext;
	private SharedPreferences mPrefs;

	// Facebook user variables
	private String mUserName;
	private String mAccessToken;

	private BeeeOnFacebook(Context context) {
		mContext = context;
		mPrefs = Controller.getInstance(mContext).getUserSettings();
		mAccessToken = mPrefs.getString(Constants.PERSISTANCE_PREF_LOGIN_FACEBOOK, null);
	}

	public static BeeeOnFacebook getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new BeeeOnFacebook(context);
		}
		return mInstance;
	}

	public String getUserName() {return mUserName;}
	public void setToken(String token) {this.mAccessToken = token;}
	public boolean isPaired() {return mAccessToken != null;}

	public void logOut() {
		if(mUserName != null)
		  Toast.makeText(mContext, mContext.getString(R.string.logout_success), Toast.LENGTH_LONG).show();
		LoginManager.getInstance().logOut();
		mUserName = null;
	}

	public void logIn(FragmentActivity activity) {
		LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile"));
	}

	// just for testing
	public void forget() {
		//TODO remove this function
		mPrefs.edit().putString(Constants.PERSISTANCE_PREF_LOGIN_FACEBOOK, null).apply();
		mUserName = null;
		mAccessToken = null;
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
					if(response.getError().getErrorCode() == -1)
						notifyObservers("connect_error");
					else
						notifyObservers("not_logged");
				}
				else {
					try {
						mUserName = object.getString("name");
						setChanged();
						notifyObservers("userName");
					}
					catch(JSONException e) {
						Log.e(TAG, "FB JSON parse error: "+e.getMessage());
					}
				}
			}
		});
		request.executeAsync();
	}

	public ShareLinkContent shareAchievement(Context context, String title, String date) {
		if (ShareDialog.canShow(ShareLinkContent.class)) {
			return new ShareLinkContent.Builder()
					.setContentTitle(title)
					.setContentDescription(date + " " + context.getString(R.string.achievement_share_msg))
					.setContentUrl(Uri.parse(context.getString(R.string.achievement_share_url)))
					.setImageUrl(Uri.parse(context.getString(R.string.achievement_share_img)))
					.build();
		}
		return null;
	}

	// TODO Download users profile picture from Facebook
//	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//		public DownloadImageTask() {}
//		protected Bitmap doInBackground(String... urls) {
//			String urldisplay = urls[0];
//			Bitmap mIcon11 = null;
//			try {
//				InputStream in = new java.net.URL(urldisplay).openStream();
//				mIcon11 = BitmapFactory.decodeStream(in);
//			} catch (Exception e) {
//				Log.e("Error", e.getMessage());
//				e.printStackTrace();
//			}
//			return mIcon11;
//		}
//		protected void onPostExecute(Bitmap result) {
//			setChanged();
//			notifyObservers("profilePicture");
//		}
//	}
}
