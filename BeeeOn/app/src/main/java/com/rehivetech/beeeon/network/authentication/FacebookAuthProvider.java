package com.rehivetech.beeeon.network.authentication;

import android.content.Context;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.rehivetech.beeeon.gui.activity.LoginActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FacebookAuthProvider implements IAuthProvider {

	// This ID must be unique amongst all providers
	public static final int PROVIDER_ID = 202;

	private static final String PROVIDER_NAME = "facebook-android";

	private static final String PARAMETER_TOKEN = "accessToken";

	private static final String AUTH_INTENT_DATA_TOKEN = "token";

	private static Map<String, String> mParameters = new HashMap<>();

	public void processResult(final LoginActivity activity, int requestCode, int resultCode, Intent data) {
		CallbackManager fbCallbackManager = CallbackManager.Factory.create();

		// Register callbackManager to handle data which we received in LoginActivity.OnActivityResult() and notify own AuthProvider results
		LoginManager.getInstance().registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				Intent authIntent = new Intent();
				authIntent.putExtra(AUTH_INTENT_DATA_TOKEN, loginResult.getAccessToken().getToken());
				activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_AUTH, authIntent);
			}

			@Override
			public void onCancel() {
				activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_CANCEL, null);
			}

			@Override
			public void onError(FacebookException exception) {
				activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_ERROR, null);
			}
		});

		fbCallbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean isDemo() {
		return false;
	}

	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	@Override
	public Map<String, String> getParameters() {
		return mParameters;
	}

	@Override
	public void setTokenParameter(String tokenParameter) {

	}

	@Override
	public boolean loadAuthIntent(Context context, Intent data) {
		String token = data.getStringExtra(AUTH_INTENT_DATA_TOKEN);
		if (token == null)
			return false;

		mParameters.put(PARAMETER_TOKEN, token);
		return true;
	}

	@Override
	public void prepareAuth(LoginActivity activity) {
		// Initialize sdk (it is required)
//		if (!FacebookSdk.isInitialized())
//			FacebookSdk.sdkInitialize(activity);

		// Do login, it will open Facebook's activity and then send result to our LoginActivity
		LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"));
	}
}
